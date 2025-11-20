package net.mooctest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ElevatorManagerTest {

    private static class FastElevator extends Elevator {
        private int doorOpened;

        FastElevator(int id, Scheduler scheduler) {
            super(id, scheduler);
        }

        @Override
        public void openDoor() {
            doorOpened++;
            setStatus(ElevatorStatus.STOPPED);
            unloadPassengers();
            loadPassengers();
        }

        @Override
        public void moveToFirstFloor() {
            setCurrentFloor(1);
            setStatus(ElevatorStatus.IDLE);
        }

        int getDoorOpened() {
            return doorOpened;
        }
    }

    private static class StubScheduler extends Scheduler {
        private List<PassengerRequest> queued = new ArrayList<>();
        private final List<PassengerRequest> dispatched = new ArrayList<>();

        StubScheduler() {
            super(new ArrayList<Elevator>(), 10, new NearestElevatorStrategy());
        }

        void setQueued(List<PassengerRequest> requests) {
            this.queued = requests;
        }

        List<PassengerRequest> getDispatched() {
            return dispatched;
        }

        @Override
        public List<PassengerRequest> getRequestsAtFloor(int floorNumber, Direction direction) {
            return new ArrayList<>(queued);
        }

        @Override
        public void dispatchElevator(PassengerRequest request) {
            dispatched.add(request);
        }
    }

    private static class MaintenanceManagerStub extends MaintenanceManager {
        MaintenanceManagerStub() {
            super();
        }

        @Override
        public void processTasks() {
            // 覆盖后台线程逻辑，避免真实循环
        }
    }

    @Before
    public void resetEnvironment() throws Exception {
        resetSingleton(SystemConfig.class);
        resetSingleton(Scheduler.class);
        resetSingleton(NotificationService.class);
        resetSingleton(LogManager.class);
        resetSingleton(AnalyticsEngine.class);
        resetSingleton(MaintenanceManager.class);
        resetSingleton(SecurityMonitor.class);
        resetSingleton(ThreadPoolManager.class);
        resetSingleton(EventBus.class);
        resetSingleton(ElevatorManager.class);

        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(20);
        config.setElevatorCount(4);
        config.setMaxLoad(800.0);
    }

    private void resetSingleton(Class<?> clazz) throws Exception {
        try {
            Field field = clazz.getDeclaredField("instance");
            field.setAccessible(true);
            Object current = field.get(null);
            if (current != null) {
                if (current instanceof ThreadPoolManager) {
                    ((ThreadPoolManager) current).shutdown();
                }
                if (current instanceof MaintenanceManager) {
                    Field execField = MaintenanceManager.class.getDeclaredField("executorService");
                    execField.setAccessible(true);
                    ExecutorService executor = (ExecutorService) execField.get(current);
                    executor.shutdownNow();
                }
            }
            field.set(null, null);
        } catch (NoSuchFieldException ignored) {
            // 非单例类，无需处理
        }
    }

    private void setSchedulerSingleton(Scheduler scheduler) throws Exception {
        Field field = Scheduler.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, scheduler);
    }

    private List<PassengerRequest> getInternalPassengerList(Elevator elevator) throws Exception {
        Field field = Elevator.class.getDeclaredField("passengerList");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> list = (List<PassengerRequest>) field.get(elevator);
        return list;
    }

    private Queue<PassengerRequest> getHighPriorityQueue(Scheduler scheduler) throws Exception {
        Field field = Scheduler.class.getDeclaredField("highPriorityQueue");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Queue<PassengerRequest> queue = (Queue<PassengerRequest>) field.get(scheduler);
        return queue;
    }

    // ======================== PassengerRequest 与 Floor ========================

    @Test(timeout = 3000)
    public void testPassengerRequestDirection() {
        // 验证乘客请求根据楼层大小设置方向
        PassengerRequest upRequest = new PassengerRequest(1, 5, Priority.HIGH, RequestType.STANDARD);
        assertEquals(Direction.UP, upRequest.getDirection());

        PassengerRequest downRequest = new PassengerRequest(10, 3, Priority.LOW, RequestType.DESTINATION_CONTROL);
        assertEquals(Direction.DOWN, downRequest.getDirection());
    }

    @Test(timeout = 3000)
    public void testFloorQueuesBothDirections() {
        // 验证楼层队列能同时存储上下行请求
        Floor floor = new Floor(5);
        PassengerRequest up = new PassengerRequest(5, 9, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest down = new PassengerRequest(5, 2, Priority.MEDIUM, RequestType.STANDARD);
        floor.addRequest(up);
        floor.addRequest(down);

        assertEquals(1, floor.getRequests(Direction.UP).size());
        assertEquals(1, floor.getRequests(Direction.DOWN).size());
        assertTrue(floor.getRequests(Direction.UP).isEmpty());
    }

    // ======================== Elevator 核心逻辑 ========================

    @Test(timeout = 3000)
    public void testUpdateDirectionSwitchesProperly() {
        // 验证updateDirection能根据最小目标楼层切换方向
        StubScheduler scheduler = new StubScheduler();
        FastElevator elevator = new FastElevator(1, scheduler);
        elevator.setCurrentFloor(5);
        elevator.addDestination(9);
        elevator.updateDirection();
        assertEquals(Direction.UP, elevator.getDirection());

        elevator.addDestination(2);
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test(timeout = 3000)
    public void testMoveConsumesDestinationAndOpensDoor() throws Exception {
        // 验证move到达目标后会开门并清理目标
        StubScheduler scheduler = new StubScheduler();
        FastElevator elevator = new FastElevator(2, scheduler);
        elevator.setCurrentFloor(3);
        elevator.addDestination(4);
        elevator.move();
        assertEquals(4, elevator.getCurrentFloor());
        assertTrue(elevator.getDestinationSet().isEmpty());
        assertEquals(1, elevator.getDoorOpened());
    }

    @Test(timeout = 3000)
    public void testLoadPassengersRespectsMaxLoad() {
        // 验证loadPassengers在达到最大载重前会拒绝更多乘客
        SystemConfig.getInstance().setMaxLoad(140.0);
        StubScheduler scheduler = new StubScheduler();
        FastElevator elevator = new FastElevator(3, scheduler);
        elevator.setCurrentFloor(1);
        elevator.setCurrentLoad(140.0);
        scheduler.setQueued(Arrays.asList(
                new PassengerRequest(1, 5, Priority.LOW, RequestType.STANDARD),
                new PassengerRequest(1, 6, Priority.LOW, RequestType.STANDARD)));

        elevator.loadPassengers();
        assertTrue(elevator.getPassengerList().isEmpty());

        elevator.setCurrentLoad(70.0);
        elevator.loadPassengers();
        assertEquals(2, elevator.getPassengerList().size());
    }

    @Test(timeout = 3000)
    public void testUnloadPassengersMatchesCurrentFloor() throws Exception {
        // 验证unloadPassengers只移除当前楼层的乘客
        StubScheduler scheduler = new StubScheduler();
        FastElevator elevator = new FastElevator(4, scheduler);
        elevator.setCurrentFloor(8);
        List<PassengerRequest> internal = getInternalPassengerList(elevator);
        PassengerRequest stay = new PassengerRequest(2, 9, Priority.HIGH, RequestType.STANDARD);
        internal.add(new PassengerRequest(1, 8, Priority.HIGH, RequestType.STANDARD));
        internal.add(stay);

        elevator.unloadPassengers();
        assertEquals(1, elevator.getPassengerList().size());
        assertEquals(stay, elevator.getPassengerList().get(0));
    }

    @Test(timeout = 3000)
    public void testClearAllRequestsReturnsSnapshot() throws Exception {
        // 验证clearAllRequests返回快照并清空内部集合
        StubScheduler scheduler = new StubScheduler();
        FastElevator elevator = new FastElevator(5, scheduler);
        getInternalPassengerList(elevator).add(new PassengerRequest(2, 7, Priority.MEDIUM, RequestType.STANDARD));
        elevator.addDestination(7);

        List<PassengerRequest> pending = elevator.clearAllRequests();
        assertEquals(1, pending.size());
        assertTrue(elevator.getPassengerList().isEmpty());
        assertTrue(elevator.getDestinationSet().isEmpty());
    }

    @Test(timeout = 3000)
    public void testHandleEmergencyClearsState() throws Exception {
        // 验证handleEmergency会清空乘客并加入1层目标
        StubScheduler scheduler = new StubScheduler();
        FastElevator elevator = new FastElevator(6, scheduler);
        getInternalPassengerList(elevator).add(new PassengerRequest(2, 9, Priority.LOW, RequestType.STANDARD));
        elevator.addDestination(9);

        elevator.handleEmergency();
        assertTrue(elevator.getPassengerList().isEmpty());
        assertTrue(elevator.getDestinationSet().contains(1));
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
    }

    @Test(timeout = 3000)
    public void testNotifyObserversWithCustomEvent() {
        // 验证自定义观察者会收到事件数据
        StubScheduler scheduler = new StubScheduler();
        FastElevator elevator = new FastElevator(7, scheduler);
        final List<Object> received = new ArrayList<>();
        elevator.addObserver((observable, arg) -> received.add(arg));

        Event event = new Event(EventType.EMERGENCY, "payload");
        elevator.notifyObservers(event);
        assertEquals(1, received.size());
        assertEquals("payload", ((Event) received.get(0)).getData());
    }

    // ======================== Scheduler ========================

    @Test(timeout = 3000)
    public void testSubmitHighPriorityGoesToQueue() throws Exception {
        // 验证高优先级请求进入专属队列
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(8, scheduler);
        elevators.add(elevator);

        PassengerRequest high = new PassengerRequest(2, 9, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(high);

        Queue<PassengerRequest> queue = getHighPriorityQueue(scheduler);
        assertTrue(queue.contains(high));
    }

    @Test(timeout = 3000)
    public void testSubmitNormalPriorityFeedsFloorQueue() {
        // 验证普通优先级请求进入楼层队列并可被读取
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(9, scheduler);
        elevators.add(elevator);

        PassengerRequest normal = new PassengerRequest(3, 8, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(normal);
        List<PassengerRequest> fetched = scheduler.getRequestsAtFloor(3, Direction.UP);
        assertEquals(1, fetched.size());
        assertEquals(normal, fetched.get(0));
    }

    @Test(timeout = 3000)
    public void testUpdateRedistributesOnFault() throws Exception {
        // 验证update在故障事件中会重新分配乘客
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        FastElevator faulty = new FastElevator(10, scheduler);
        FastElevator healthy = new FastElevator(11, scheduler);
        elevators.add(faulty);
        elevators.add(healthy);

        getInternalPassengerList(faulty).add(new PassengerRequest(1, 6, Priority.LOW, RequestType.STANDARD));
        faulty.addDestination(6);

        scheduler.update(faulty, new Event(EventType.ELEVATOR_FAULT, null));
        assertTrue(faulty.getPassengerList().isEmpty());
    }

    @Test(timeout = 3000)
    public void testExecuteEmergencyProtocolFromUpdate() {
        // 验证EMERGENCY事件会触发紧急协议
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(12, scheduler);
        elevators.add(elevator);

        scheduler.update(elevator, new Event(EventType.EMERGENCY, null));
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
    }

    // ======================== 调度策略 ========================

    @Test(timeout = 3000)
    public void testNearestStrategyPrefersCloserEligibleElevator() {
        // 验证最近电梯策略根据距离选择电梯
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        FastElevator near = new FastElevator(13, scheduler);
        near.setCurrentFloor(3);
        near.setStatus(ElevatorStatus.IDLE);
        FastElevator far = new FastElevator(14, scheduler);
        far.setCurrentFloor(10);
        far.setStatus(ElevatorStatus.IDLE);

        PassengerRequest request = new PassengerRequest(4, 8, Priority.MEDIUM, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(near, far), request);
        assertEquals(near, chosen);
    }

    @Test(timeout = 3000)
    public void testHighEfficiencyStrategyIsCloserBranch() {
        // 验证高效率策略的距离比较分支
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        FastElevator candidate = new FastElevator(15, scheduler);
        candidate.setCurrentFloor(5);
        FastElevator current = new FastElevator(16, scheduler);
        current.setCurrentFloor(9);
        PassengerRequest req = new PassengerRequest(6, 9, Priority.LOW, RequestType.STANDARD);
        assertTrue(strategy.isCloser(candidate, current, req));
    }

    @Test(timeout = 3000)
    public void testEnergySavingStrategyPrefersIdleThenClose() {
        // 验证节能策略先选空闲，再选方向匹配且接近的电梯
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        FastElevator busy = new FastElevator(17, scheduler);
        busy.setStatus(ElevatorStatus.MOVING);
        busy.setDirection(Direction.UP);
        busy.setCurrentFloor(2);
        FastElevator idle = new FastElevator(18, scheduler);
        idle.setStatus(ElevatorStatus.IDLE);
        PassengerRequest req = new PassengerRequest(9, 12, Priority.MEDIUM, RequestType.STANDARD);
        assertEquals(idle, strategy.selectElevator(Arrays.asList(busy, idle), req));

        idle.setStatus(ElevatorStatus.MOVING);
        idle.setDirection(Direction.UP);
        idle.setCurrentFloor(8);
        assertEquals(idle, strategy.selectElevator(Collections.singletonList(idle), req));
    }

    @Test(timeout = 3000)
    public void testPredictiveStrategyConsidersLoadFactor() throws Exception {
        // 验证预测策略结合距离与载重
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        FastElevator light = new FastElevator(19, scheduler);
        FastElevator heavy = new FastElevator(20, scheduler);
        getInternalPassengerList(heavy).add(new PassengerRequest(1, 2, Priority.LOW, RequestType.STANDARD));
        heavy.setCurrentLoad(400.0);
        light.setCurrentFloor(6);
        heavy.setCurrentFloor(5);

        PassengerRequest req = new PassengerRequest(6, 12, Priority.MEDIUM, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(light, heavy), req);
        assertEquals(light, chosen);
    }

    // ======================== SystemConfig ========================

    @Test(timeout = 3000)
    public void testSystemConfigValidation() {
        // 验证配置只接受正值
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(-1);
        assertEquals(20, config.getFloorCount());
        config.setElevatorCount(6);
        assertEquals(6, config.getElevatorCount());
        config.setMaxLoad(0);
        assertEquals(800.0, config.getMaxLoad(), 0.001);
    }

    // ======================== Notification 与 Log/Analytics ========================

    @Test(timeout = 3000)
    public void testSmsChannelSupportsOnlyCriticalTypes() {
        // 验证短信通道只支持紧急与维护通知
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        assertTrue(channel.supports(NotificationService.NotificationType.EMERGENCY));
        assertTrue(channel.supports(NotificationService.NotificationType.MAINTENANCE));
        assertFalse(channel.supports(NotificationService.NotificationType.INFORMATION));
    }

    @Test(timeout = 3000)
    public void testLogManagerQueryFiltersBySource() {
        // 验证日志查询会按来源过滤
        LogManager manager = LogManager.getInstance();
        long start = System.currentTimeMillis();
        manager.recordEvent("A", "first");
        manager.recordEvent("B", "second");
        long end = System.currentTimeMillis() + 5;
        List<LogManager.SystemLog> logs = manager.queryLogs("A", start, end);
        assertEquals(1, logs.size());
        assertEquals("first", logs.get(0).getMessage());
    }

    @Test(timeout = 3000)
    public void testAnalyticsEngineDetectsPeakHours() {
        // 验证AnalyticsEngine通过stream求和判断高峰
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        engine.updateFloorPassengerCount(1, 30);
        engine.updateFloorPassengerCount(2, 25);
        assertTrue(engine.isPeakHours());
        engine.updateFloorPassengerCount(3, 0);
        assertTrue(engine.isPeakHours());
    }

    // ======================== MaintenanceManager 与 SecurityMonitor ========================

    @Test(timeout = 3000)
    public void testMaintenanceManagerKeepsRecords() throws Exception {
        // 验证维护管理器记录维护结果
        MaintenanceManagerStub manager = new MaintenanceManagerStub();
        manager.recordMaintenanceResult(1, "done");

        Field field = MaintenanceManager.class.getDeclaredField("maintenanceRecords");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<MaintenanceManager.MaintenanceRecord> records = (List<MaintenanceManager.MaintenanceRecord>) field.get(manager);
        assertEquals(1, records.size());
    }

    @Test(timeout = 3000)
    public void testScheduleMaintenanceAddsTask() throws Exception {
        // 验证scheduleMaintenance会写入任务队列
        MaintenanceManagerStub manager = new MaintenanceManagerStub();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(21, scheduler);
        manager.scheduleMaintenance(elevator);

        Field field = MaintenanceManager.class.getDeclaredField("taskQueue");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Queue<MaintenanceManager.MaintenanceTask> queue = (Queue<MaintenanceManager.MaintenanceTask>) field.get(manager);
        assertEquals(1, queue.size());
    }

    @Test(timeout = 3000)
    public void testSecurityMonitorTriggersEmergencyProtocol() throws Exception {
        // 验证SecurityMonitor在接收紧急事件时调用调度器紧急流程
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 5, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(22, scheduler);
        elevators.add(elevator);
        setSchedulerSingleton(scheduler);

        SecurityMonitor monitor = SecurityMonitor.getInstance();
        monitor.onEvent(new EventBus.Event(EventType.EMERGENCY, "alarm"));
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
    }

    // ======================== EventBus 与 ThreadPoolManager ========================

    @Test(timeout = 3000)
    public void testEventBusPublishNotifiesSubscriber() {
        // 验证事件总线的订阅发布机制
        EventBus bus = EventBus.getInstance();
        final List<Object> payloads = new ArrayList<>();
        bus.subscribe(EventType.CONFIG_UPDATED, event -> payloads.add(event.getData()));
        bus.publish(new EventBus.Event(EventType.CONFIG_UPDATED, "cfg"));
        assertEquals(Collections.singletonList("cfg"), payloads);
    }

    @Test(timeout = 3000)
    public void testThreadPoolManagerExecutesTask() {
        // 验证线程池能执行异步任务
        ThreadPoolManager manager = new ThreadPoolManager();
        CountDownLatch latch = new CountDownLatch(1);
        manager.submitTask(latch::countDown);
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("等待线程完成超时");
        } finally {
            manager.shutdown();
        }
    }

    // ======================== ElevatorManager ========================

    @Test(timeout = 3000)
    public void testElevatorManagerRegistersElevators() {
        // 验证ElevatorManager可以注册并返回电梯
        ElevatorManager manager = ElevatorManager.getInstance();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(23, scheduler);
        manager.registerElevator(elevator);
        assertEquals(elevator, manager.getElevatorById(23));
        assertTrue(manager.getAllElevators().contains(elevator));
    }
}
