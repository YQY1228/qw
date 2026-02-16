package net.mooctest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ElevatorManagerTest {

    @Before
    public void resetEnvironment() throws Exception {
        Class<?>[] singletons = new Class<?>[] {
                SystemConfig.class,
                Scheduler.class,
                NotificationService.class,
                LogManager.class,
                AnalyticsEngine.class,
                MaintenanceManager.class,
                SecurityMonitor.class,
                ThreadPoolManager.class,
                EventBus.class,
                ElevatorManager.class
        };
        for (Class<?> clazz : singletons) {
            resetSingleton(clazz);
        }
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(20);
        config.setElevatorCount(4);
        config.setMaxLoad(800.0);
    }

    private void resetSingleton(Class<?> clazz) throws Exception {
        try {
            Field instanceField = clazz.getDeclaredField("instance");
            instanceField.setAccessible(true);
            Object current = instanceField.get(null);
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
            instanceField.set(null, null);
        } catch (NoSuchFieldException ignored) {
            // 忽略没有单例字段的类
        }
    }

    @SuppressWarnings("unchecked")
    private List<PassengerRequest> internalPassengers(Elevator elevator) throws Exception {
        Field field = Elevator.class.getDeclaredField("passengerList");
        field.setAccessible(true);
        return (List<PassengerRequest>) field.get(elevator);
    }

    @SuppressWarnings("unchecked")
    private Queue<PassengerRequest> highPriorityQueue(Scheduler scheduler) throws Exception {
        Field field = Scheduler.class.getDeclaredField("highPriorityQueue");
        field.setAccessible(true);
        return (Queue<PassengerRequest>) field.get(scheduler);
    }

    @SuppressWarnings("unchecked")
    private Queue<MaintenanceManager.MaintenanceTask> maintenanceQueue(MaintenanceManager manager) throws Exception {
        Field field = MaintenanceManager.class.getDeclaredField("taskQueue");
        field.setAccessible(true);
        return (Queue<MaintenanceManager.MaintenanceTask>) field.get(manager);
    }

    @SuppressWarnings("unchecked")
    private List<MaintenanceManager.MaintenanceRecord> maintenanceRecords(MaintenanceManager manager) throws Exception {
        Field field = MaintenanceManager.class.getDeclaredField("maintenanceRecords");
        field.setAccessible(true);
        return (List<MaintenanceManager.MaintenanceRecord>) field.get(manager);
    }

    private void stopMaintenanceExecutor(MaintenanceManager manager) throws Exception {
        Field execField = MaintenanceManager.class.getDeclaredField("executorService");
        execField.setAccessible(true);
        ExecutorService executor = (ExecutorService) execField.get(manager);
        executor.shutdownNow();
    }

    private void setSchedulerSingleton(Scheduler scheduler) throws Exception {
        Field instanceField = Scheduler.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, scheduler);
    }

    @Test(timeout = 4000)
    public void testEnumsCoverage() {
        // 验证所有枚举类型的取值完整性与valueOf兼容性
        assertEquals(2, Direction.values().length);
        assertEquals(Direction.UP, Direction.valueOf("UP"));
        assertEquals(6, ElevatorStatus.values().length);
        assertEquals(ElevatorMode.ENERGY_SAVING, ElevatorMode.valueOf("ENERGY_SAVING"));
        assertEquals(3, Priority.values().length);
        assertEquals(RequestType.DESTINATION_CONTROL, RequestType.valueOf("DESTINATION_CONTROL"));
        assertEquals(SpecialNeeds.VIP_SERVICE, SpecialNeeds.valueOf("VIP_SERVICE"));
        assertEquals(EventType.CONFIG_UPDATED, EventType.valueOf("CONFIG_UPDATED"));
        assertEquals(4, NotificationService.NotificationType.values().length);
    }

    @Test(timeout = 4000)
    public void testSystemConfigValidation() {
        // 验证SystemConfig对正值、零值和负值输入的处理逻辑
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(30);
        config.setElevatorCount(6);
        config.setMaxLoad(900.0);
        assertEquals(30, config.getFloorCount());
        assertEquals(6, config.getElevatorCount());
        assertEquals(900.0, config.getMaxLoad(), 0.001);

        config.setFloorCount(0);
        config.setElevatorCount(0);
        config.setMaxLoad(0.0);
        assertEquals(30, config.getFloorCount());
        assertEquals(6, config.getElevatorCount());
        assertEquals(900.0, config.getMaxLoad(), 0.001);

        config.setFloorCount(-5);
        config.setElevatorCount(-1);
        config.setMaxLoad(-50.0);
        assertEquals(30, config.getFloorCount());
        assertEquals(6, config.getElevatorCount());
        assertEquals(900.0, config.getMaxLoad(), 0.001);
    }

    @Test(timeout = 4000)
    public void testPassengerRequestBehaviors() {
        // 验证PassengerRequest根据楼层计算方向并正确输出字符串
        PassengerRequest up = new PassengerRequest(1, 10, Priority.HIGH, RequestType.STANDARD);
        PassengerRequest down = new PassengerRequest(10, 1, Priority.LOW, RequestType.DESTINATION_CONTROL);
        PassengerRequest same = new PassengerRequest(5, 5, Priority.MEDIUM, RequestType.STANDARD);
        assertEquals(Direction.UP, up.getDirection());
        assertEquals(Direction.DOWN, down.getDirection());
        assertEquals(Direction.DOWN, same.getDirection());
        String describe = up.toString();
        assertTrue(describe.contains("1"));
        assertTrue(describe.contains("10"));
    }

    @Test(timeout = 4000)
    public void testEventDataUsage() {
        // 验证Event可以携带和返回不同的数据内容
        Event normal = new Event(EventType.EMERGENCY, "payload");
        Event empty = new Event(EventType.ELEVATOR_FAULT, null);
        assertEquals("payload", normal.getData());
        assertNull(empty.getData());
    }

    @Test(timeout = 4000)
    public void testFloorQueueFlow() {
        // 验证Floor按照方向维护队列并在读取后清空
        Floor floor = new Floor(6);
        PassengerRequest up = new PassengerRequest(6, 9, Priority.HIGH, RequestType.STANDARD);
        PassengerRequest down = new PassengerRequest(6, 2, Priority.MEDIUM, RequestType.STANDARD);
        floor.addRequest(up);
        floor.addRequest(down);
        assertEquals(1, floor.getRequests(Direction.UP).size());
        assertEquals(1, floor.getRequests(Direction.DOWN).size());
        assertTrue(floor.getRequests(Direction.UP).isEmpty());
    }

    @Test(timeout = 4000)
    public void testElevatorDirectionDecisions() {
        // 验证updateDirection在不同目标集合下的方向判定
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 15, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        elevator.addDestination(8);
        elevator.updateDirection();
        assertEquals(Direction.UP, elevator.getDirection());

        elevator.addDestination(2);
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());

        elevator.getDestinationSet().clear();
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.updateDirection();
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }

    @Test(timeout = 4000)
    public void testElevatorMovementAndDoorFlow() throws Exception {
        // 验证move方法在抵达目的楼层时会开门并最终回到空闲状态
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 20, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler) {
            @Override
            public void openDoor() {
                setStatus(ElevatorStatus.STOPPED);
            }
        };
        elevator.setCurrentFloor(3);
        elevator.setDirection(Direction.UP);
        elevator.addDestination(4);
        elevator.addDestination(6);
        elevator.move();
        assertEquals(4, elevator.getCurrentFloor());
        assertTrue(elevator.getEnergyConsumption() > 0);
        elevator.move();
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }

    @Test(timeout = 4000)
    public void testElevatorLoadUnloadEmergency() throws Exception {
        // 验证卸客、载客以及紧急处理逻辑
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 20, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        elevator.setCurrentFloor(5);
        List<PassengerRequest> internal = internalPassengers(elevator);
        internal.add(new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD));
        internal.add(new PassengerRequest(3, 9, Priority.LOW, RequestType.STANDARD));
        elevator.unloadPassengers();
        assertEquals(1, internalPassengers(elevator).size());

        elevator.setCurrentLoad(elevator.getMaxLoad());
        scheduler.submitRequest(new PassengerRequest(5, 9, Priority.HIGH, RequestType.STANDARD));
        elevator.loadPassengers();
        assertEquals(1, internalPassengers(elevator).size());
        elevator.setCurrentLoad(0.0);
        scheduler.submitRequest(new PassengerRequest(5, 10, Priority.LOW, RequestType.STANDARD));
        elevator.loadPassengers();
        assertTrue(internalPassengers(elevator).size() >= 1);

        elevator.handleEmergency();
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
        assertTrue(elevator.getDestinationSet().contains(1));
    }

    @Test(timeout = 4000)
    public void testElevatorClearRequestsAndObservers() throws Exception {
        // 验证清空请求与观察者通知流程
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        internalPassengers(elevator).add(new PassengerRequest(1, 7, Priority.HIGH, RequestType.STANDARD));
        elevator.addDestination(7);
        List<PassengerRequest> snapshot = elevator.clearAllRequests();
        assertEquals(1, snapshot.size());
        assertTrue(elevator.getDestinationSet().isEmpty());

        final boolean[] called = {false};
        elevator.addObserver((observable, arg) -> called[0] = true);
        elevator.notifyObservers(new Event(EventType.EMERGENCY, "alert"));
        assertTrue(called[0]);
    }

    @Test(timeout = 4000)
    public void testSchedulerSubmitAndQueues() throws Exception {
        // 验证Scheduler提交请求时高优先级进入专队并普通请求进楼层队列
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 12, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        elevators.add(elevator);

        PassengerRequest high = new PassengerRequest(2, 9, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(high);
        assertTrue(highPriorityQueue(scheduler).contains(high));

        PassengerRequest normal = new PassengerRequest(3, 8, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(normal);
        List<PassengerRequest> fetched = scheduler.getRequestsAtFloor(3, Direction.UP);
        assertEquals(1, fetched.size());
    }

    @Test(timeout = 4000)
    public void testSchedulerDispatchAndRequests() {
        // 验证调度成功时目的楼层被加入并可再次获取队列请求
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 15, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        elevators.add(elevator);

        PassengerRequest request = new PassengerRequest(4, 11, Priority.LOW, RequestType.STANDARD);
        scheduler.dispatchElevator(request);
        assertTrue(elevator.getDestinationSet().contains(4));

        scheduler.submitRequest(new PassengerRequest(6, 12, Priority.MEDIUM, RequestType.STANDARD));
        assertEquals(1, scheduler.getRequestsAtFloor(6, Direction.UP).size());
    }

    @Test(timeout = 4000)
    public void testSchedulerUpdateAndProtocols() throws Exception {
        // 验证update在故障与紧急事件下的分支
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 20, new NearestElevatorStrategy());
        Elevator faulty = new Elevator(1, scheduler);
        Elevator healthy = new Elevator(2, scheduler);
        healthy.setStatus(ElevatorStatus.IDLE);
        elevators.add(faulty);
        elevators.add(healthy);

        internalPassengers(faulty).add(new PassengerRequest(1, 7, Priority.HIGH, RequestType.STANDARD));
        faulty.addDestination(7);
        scheduler.update(faulty, new Event(EventType.ELEVATOR_FAULT, null));
        assertTrue(internalPassengers(faulty).isEmpty());

        scheduler.update(faulty, new Event(EventType.EMERGENCY, null));
        assertEquals(ElevatorStatus.EMERGENCY, faulty.getStatus());
        assertEquals(ElevatorStatus.EMERGENCY, healthy.getStatus());
    }

    @Test(timeout = 4000)
    public void testSchedulerStrategySwitching() {
        // 验证更换调度策略后仍能调用自定义逻辑
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        elevators.add(elevator);

        final boolean[] used = {false};
        scheduler.setDispatchStrategy((els, req) -> {
            used[0] = true;
            return els.isEmpty() ? null : els.get(0);
        });
        scheduler.dispatchElevator(new PassengerRequest(2, 9, Priority.MEDIUM, RequestType.STANDARD));
        assertTrue(used[0]);
    }

    @Test(timeout = 4000)
    public void testNearestStrategyEligibility() {
        // 验证最近电梯策略的选择与资格判断
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator idle = new Elevator(1, scheduler);
        idle.setStatus(ElevatorStatus.IDLE);
        Elevator movingSame = new Elevator(2, scheduler);
        movingSame.setStatus(ElevatorStatus.MOVING);
        movingSame.setDirection(Direction.UP);
        movingSame.setCurrentFloor(8);
        Elevator movingDifferent = new Elevator(3, scheduler);
        movingDifferent.setStatus(ElevatorStatus.MOVING);
        movingDifferent.setDirection(Direction.DOWN);
        movingDifferent.setCurrentFloor(2);
        PassengerRequest req = new PassengerRequest(6, 12, Priority.HIGH, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(idle, movingSame), req);
        assertEquals(idle, chosen);
        assertTrue(strategy.isEligible(idle, req));
        assertTrue(strategy.isEligible(movingSame, req));
        assertFalse(strategy.isEligible(movingDifferent, req));
    }

    @Test(timeout = 4000)
    public void testHighEfficiencyStrategyComparison() {
        // 验证高效策略在距离比较和选梯时的行为
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator near = new Elevator(1, scheduler);
        near.setCurrentFloor(4);
        near.setStatus(ElevatorStatus.IDLE);
        Elevator far = new Elevator(2, scheduler);
        far.setCurrentFloor(12);
        far.setStatus(ElevatorStatus.IDLE);
        PassengerRequest req = new PassengerRequest(5, 9, Priority.LOW, RequestType.STANDARD);
        assertEquals(near, strategy.selectElevator(Arrays.asList(near, far), req));
        assertTrue(strategy.isCloser(near, far, req));
        assertFalse(strategy.isCloser(far, near, req));
    }

    @Test(timeout = 4000)
    public void testEnergySavingStrategyBranches() {
        // 验证节能策略对空闲、电梯方向及距离的判定
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator idle = new Elevator(1, scheduler);
        idle.setStatus(ElevatorStatus.IDLE);
        Elevator close = new Elevator(2, scheduler);
        close.setStatus(ElevatorStatus.MOVING);
        close.setDirection(Direction.UP);
        close.setCurrentFloor(9);
        Elevator far = new Elevator(3, scheduler);
        far.setStatus(ElevatorStatus.MOVING);
        far.setDirection(Direction.UP);
        far.setCurrentFloor(2);
        Elevator wrongDir = new Elevator(4, scheduler);
        wrongDir.setStatus(ElevatorStatus.MOVING);
        wrongDir.setDirection(Direction.DOWN);
        wrongDir.setCurrentFloor(8);
        PassengerRequest req = new PassengerRequest(10, 15, Priority.MEDIUM, RequestType.STANDARD);
        assertEquals(idle, strategy.selectElevator(Arrays.asList(idle, close), req));
        assertEquals(close, strategy.selectElevator(Arrays.asList(close), req));
        assertNull(strategy.selectElevator(Arrays.asList(far), req));
        assertNull(strategy.selectElevator(Arrays.asList(wrongDir), req));
        assertNull(strategy.selectElevator(new ArrayList<>(), req));
    }

    @Test(timeout = 4000)
    public void testPredictiveStrategyCosting() throws Exception {
        // 验证预测调度策略在距离与载重因子下的选梯逻辑
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator light = new Elevator(1, scheduler);
        light.setCurrentFloor(6);
        Elevator heavy = new Elevator(2, scheduler);
        heavy.setCurrentFloor(5);
        internalPassengers(heavy).add(new PassengerRequest(1, 2, Priority.LOW, RequestType.STANDARD));
        heavy.setCurrentLoad(400.0);
        PassengerRequest req = new PassengerRequest(6, 12, Priority.HIGH, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(light, heavy), req);
        assertEquals(light, chosen);
        assertTrue(strategy.calculatePredictedCost(light, req) < strategy.calculatePredictedCost(heavy, req));
        assertNull(strategy.selectElevator(new ArrayList<>(), req));
    }

    @Test(timeout = 4000)
    public void testElevatorManagerOperations() {
        // 验证ElevatorManager注册与查询功能
        ElevatorManager manager = ElevatorManager.getInstance();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 8, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(101, scheduler);
        manager.registerElevator(elevator);
        assertEquals(elevator, manager.getElevatorById(101));
        assertTrue(manager.getAllElevators().contains(elevator));
        assertNull(manager.getElevatorById(999));
    }

    @Test(timeout = 4000)
    public void testNotificationServiceAndChannels() {
        // 验证通知服务单例以及渠道的支持类型
        NotificationService service = NotificationService.getInstance();
        NotificationService.Notification emergency = new NotificationService.Notification(
                NotificationService.NotificationType.EMERGENCY,
                "alert",
                Arrays.asList("u1@demo"));
        NotificationService.Notification info = new NotificationService.Notification(
                NotificationService.NotificationType.INFORMATION,
                "info",
                Arrays.asList("u2@demo"));
        service.sendNotification(emergency);
        service.sendNotification(info);
        NotificationService.SMSChannel sms = new NotificationService.SMSChannel();
        assertTrue(sms.supports(NotificationService.NotificationType.EMERGENCY));
        assertFalse(sms.supports(NotificationService.NotificationType.INFORMATION));
        NotificationService.EmailChannel email = new NotificationService.EmailChannel();
        assertTrue(email.supports(NotificationService.NotificationType.SYSTEM_UPDATE));
    }

    @Test(timeout = 4000)
    public void testLogManagerFiltering() {
        // 验证LogManager能够按来源和时间过滤日志
        LogManager manager = LogManager.getInstance();
        long start = System.currentTimeMillis();
        manager.recordEvent("SRC", "first");
        manager.recordEvent("SRC", "second");
        manager.recordEvent("OTHER", "xxx");
        long end = start + 5000;
        assertEquals(2, manager.queryLogs("SRC", start, end).size());
        assertEquals(0, manager.queryLogs("NONE", start, end).size());
    }

    @Test(timeout = 4000)
    public void testAnalyticsEnginePeaks() {
        // 验证AnalyticsEngine对高峰判断与报告生成
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        engine.updateFloorPassengerCount(1, 10);
        engine.updateFloorPassengerCount(2, 15);
        assertFalse(engine.isPeakHours());
        engine.updateFloorPassengerCount(3, 30);
        assertTrue(engine.isPeakHours());
        AnalyticsEngine.Report report = engine.generatePerformanceReport();
        assertEquals("System Performance Report", report.getTitle());
        assertTrue(report.getGeneratedTime() > 0);
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerQueues() throws Exception {
        // 验证MaintenanceManager维护任务与记录列表
        MaintenanceManager manager = MaintenanceManager.getInstance();
        stopMaintenanceExecutor(manager);
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(5, scheduler);
        manager.scheduleMaintenance(elevator);
        assertEquals(1, maintenanceQueue(manager).size());
        MaintenanceManager.MaintenanceTask task = maintenanceQueue(manager).peek();
        manager.performMaintenance(task);
        manager.recordMaintenanceResult(5, "done");
        assertTrue(maintenanceRecords(manager).size() >= 1);
    }

    @Test(timeout = 4000)
    public void testSecurityMonitorEmergencyFlow() throws Exception {
        // 验证SecurityMonitor接收紧急事件后会触发调度紧急协议
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 12, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        setSchedulerSingleton(scheduler);
        SecurityMonitor monitor = SecurityMonitor.getInstance();
        monitor.handleEmergency("fire");
        monitor.onEvent(new EventBus.Event(EventType.EMERGENCY, "alarm"));
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
    }

    @Test(timeout = 4000)
    public void testThreadPoolManagerExecution() throws Exception {
        // 验证线程池能够执行任务并且支持关闭
        ThreadPoolManager manager = new ThreadPoolManager();
        CountDownLatch latch = new CountDownLatch(1);
        manager.submitTask(latch::countDown);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        manager.shutdown();
    }

    @Test(timeout = 4000)
    public void testEventBusPublishSubscribe() {
        // 验证事件总线的订阅机制与空订阅处理
        EventBus bus = EventBus.getInstance();
        final boolean[] called = {false};
        bus.subscribe(EventType.CONFIG_UPDATED, event -> called[0] = true);
        bus.publish(new EventBus.Event(EventType.CONFIG_UPDATED, "cfg"));
        assertTrue(called[0]);
        bus.publish(new EventBus.Event(EventType.MAINTENANCE_REQUIRED, "noop"));
    }

    @Test(timeout = 4000)
    public void testIntegrationCollectionsAndStreams() {
        // 验证集合排序与流求和逻辑在综合场景下正常运行
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.addDestination(9);
        elevator.addDestination(3);
        elevator.addDestination(7);
        Iterator<Integer> iterator = elevator.getDestinationSet().iterator();
        assertEquals(3, iterator.next().intValue());
        assertEquals(7, iterator.next().intValue());
        assertEquals(9, iterator.next().intValue());

        AnalyticsEngine engine = new AnalyticsEngine();
        engine.updateFloorPassengerCount(1, 20);
        engine.updateFloorPassengerCount(2, 20);
        engine.updateFloorPassengerCount(3, 20);
        assertTrue(engine.isPeakHours());
    }
}
