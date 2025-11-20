package net.mooctest;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * 测试代码基于JUnit 4，若eclipse提示未找到Junit 5的测试用例，请在Run Configurations中设置Test Runner为Junit 4。请不要使用Junit 5
 * 语法编写测试代码
 */

public class ElevatorManagerTest {

    private static final List<Class<?>> SINGLETONS = Arrays.asList(
            SystemConfig.class,
            ElevatorManager.class,
            Scheduler.class,
            EventBus.class,
            MaintenanceManager.class,
            NotificationService.class,
            AnalyticsEngine.class,
            LogManager.class,
            SecurityMonitor.class,
            ThreadPoolManager.class
    );

    @Before
    public void resetSingletons() throws Exception {
        for (Class<?> clazz : SINGLETONS) {
            resetSingletonInstance(clazz);
        }
    }

    @After
    public void cleanupExecutors() throws Exception {
        for (Class<?> clazz : Arrays.asList(MaintenanceManager.class, SecurityMonitor.class, ThreadPoolManager.class)) {
            Field instanceField;
            try {
                instanceField = clazz.getDeclaredField("instance");
            } catch (NoSuchFieldException e) {
                continue;
            }
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            if (instance != null) {
                shutdownIfNeeded(instance);
            }
        }
    }

    private void resetSingletonInstance(Class<?> clazz) throws Exception {
        try {
            Field instanceField = clazz.getDeclaredField("instance");
            instanceField.setAccessible(true);
            Object existing = instanceField.get(null);
            if (existing != null) {
                shutdownIfNeeded(existing);
            }
            instanceField.set(null, null);
        } catch (NoSuchFieldException ignored) {
            // class without singleton instance
        }
    }

    private void shutdownIfNeeded(Object target) throws Exception {
        if (target instanceof ThreadPoolManager) {
            ((ThreadPoolManager) target).shutdown();
        } else if (target instanceof MaintenanceManager) {
            shutdownExecutorField(target, "executorService");
        } else if (target instanceof SecurityMonitor) {
            shutdownExecutorField(target, "executorService");
        }
    }

    private void shutdownExecutorField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        ExecutorService executor = (ExecutorService) field.get(target);
        if (executor != null) {
            executor.shutdownNow();
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        }
    }

    // ==================== 辅助类 ====================

    private static class RecordingObserver implements Observer {
        private Object lastArg;
        @Override
        public void update(java.util.Observable o, Object arg) {
            this.lastArg = arg;
        }
    }

    private static class StubScheduler extends Scheduler {
        private final Map<Integer, Map<Direction, Queue<PassengerRequest>>> manualRequests = new EnumMap<>(Integer.class);
        private final List<PassengerRequest> dispatched = new ArrayList<>();

        StubScheduler(List<Elevator> elevators, int floors) {
            super(elevators, floors, new NearestElevatorStrategy());
        }

        void addManualRequests(int floor, Direction direction, PassengerRequest... requests) {
            manualRequests.computeIfAbsent(floor, k -> new EnumMap<>(Direction.class))
                    .computeIfAbsent(direction, k -> new ArrayDeque<>())
                    .addAll(Arrays.asList(requests));
        }

        List<PassengerRequest> getDispatched() {
            return dispatched;
        }

        @Override
        public List<PassengerRequest> getRequestsAtFloor(int floorNumber, Direction direction) {
            Map<Direction, Queue<PassengerRequest>> perDirection = manualRequests.get(floorNumber);
            if (perDirection == null) {
                return Collections.emptyList();
            }
            Queue<PassengerRequest> queue = perDirection.get(direction);
            if (queue == null) {
                return Collections.emptyList();
            }
            List<PassengerRequest> copy = new ArrayList<>(queue);
            queue.clear();
            return copy;
        }

        @Override
        public void dispatchElevator(PassengerRequest request) {
            dispatched.add(request);
        }
    }

    private static class FastElevator extends Elevator {
        FastElevator(int id, Scheduler scheduler) {
            super(id, scheduler);
        }

        @Override
        public void openDoor() {
            setStatus(ElevatorStatus.STOPPED);
            unloadPassengers();
            loadPassengers();
        }

        @Override
        public void moveToFirstFloor() {
            setCurrentFloor(1);
            setStatus(ElevatorStatus.IDLE);
        }
    }

    private static class EmergencyAwareElevator extends Elevator {
        private boolean handled;
        EmergencyAwareElevator(int id, Scheduler scheduler) {
            super(id, scheduler);
        }

        @Override
        public void handleEmergency() {
            handled = true;
        }

        boolean isHandled() {
            return handled;
        }
    }

    private static class CountingDispatchStrategy implements DispatchStrategy {
        private Elevator lastElevator;
        @Override
        public Elevator selectElevator(List<Elevator> elevators, PassengerRequest request) {
            if (elevators.isEmpty()) {
                return null;
            }
            lastElevator = elevators.get(0);
            return lastElevator;
        }

        Elevator getLastElevator() {
            return lastElevator;
        }
    }

    private static class RecordingChannel implements NotificationService.NotificationChannel {
        private final Set<NotificationService.NotificationType> supported;
        private final List<NotificationService.Notification> received = new ArrayList<>();

        RecordingChannel(NotificationService.NotificationType... supportedTypes) {
            this.supported = EnumSet.noneOf(NotificationService.NotificationType.class);
            this.supported.addAll(Arrays.asList(supportedTypes));
        }

        @Override
        public boolean supports(NotificationService.NotificationType type) {
            return supported.contains(type);
        }

        @Override
        public void send(NotificationService.Notification notification) {
            received.add(notification);
        }

        List<NotificationService.Notification> getReceived() {
            return received;
        }
    }

    private static class StubNotificationService extends NotificationService {
        private final List<Notification> sent = new ArrayList<>();
        @Override
        public void sendNotification(Notification notification) {
            sent.add(notification);
        }
        List<Notification> getSent() {
            return sent;
        }
    }

    private static class EmergencyScheduler extends Scheduler {
        private boolean emergencyTriggered;
        EmergencyScheduler() {
            super(new ArrayList<>(), 0, new NearestElevatorStrategy());
        }
        @Override
        public void executeEmergencyProtocol() {
            emergencyTriggered = true;
        }
        boolean isEmergencyTriggered() {
            return emergencyTriggered;
        }
    }

    // ==================== PassengerRequest测试 ====================

    @Test(timeout = 4000)
    public void testPassengerRequestAutoDirection() {
        // 测试乘客请求自动推断上下行方向
        PassengerRequest up = new PassengerRequest(1, 5, Priority.HIGH, RequestType.STANDARD);
        PassengerRequest down = new PassengerRequest(9, 2, Priority.MEDIUM, RequestType.DESTINATION_CONTROL);
        assertEquals(Direction.UP, up.getDirection());
        assertEquals(Direction.DOWN, down.getDirection());
        assertTrue(up.getTimestamp() <= System.currentTimeMillis());
    }

    @Test(timeout = 4000)
    public void testPassengerRequestToStringContainsInfo() {
        // 测试乘客请求的toString输出包含关键字段
        PassengerRequest request = new PassengerRequest(4, 7, Priority.LOW, RequestType.STANDARD);
        String text = request.toString();
        assertTrue(text.contains("4"));
        assertTrue(text.contains("7"));
        assertTrue(text.contains("LOW"));
    }

    // ==================== Floor测试 ====================

    @Test(timeout = 4000)
    public void testFloorStoresRequestsByDirection() {
        // 测试楼层根据方向维护队列并能正确取出
        Floor floor = new Floor(3);
        PassengerRequest up = new PassengerRequest(3, 9, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest down = new PassengerRequest(3, 1, Priority.LOW, RequestType.STANDARD);
        floor.addRequest(up);
        floor.addRequest(down);
        assertEquals(up, floor.getRequests(Direction.UP).get(0));
        assertEquals(down, floor.getRequests(Direction.DOWN).get(0));
    }

    @Test(timeout = 4000)
    public void testFloorClearsQueueAfterFetch() {
        // 测试同一方向的请求在取出后被清空
        Floor floor = new Floor(8);
        floor.addRequest(new PassengerRequest(8, 10, Priority.HIGH, RequestType.STANDARD));
        assertEquals(1, floor.getRequests(Direction.UP).size());
        assertTrue(floor.getRequests(Direction.UP).isEmpty());
    }

    // ==================== SystemConfig测试 ====================

    @Test(timeout = 4000)
    public void testSystemConfigAcceptsValidValues() {
        // 测试系统配置能够更新合法值
        SystemConfig config = new SystemConfig();
        config.setFloorCount(30);
        config.setElevatorCount(6);
        config.setMaxLoad(900);
        assertEquals(30, config.getFloorCount());
        assertEquals(6, config.getElevatorCount());
        assertEquals(900, config.getMaxLoad(), 0.01);
    }

    @Test(timeout = 4000)
    public void testSystemConfigRejectsInvalidValues() {
        // 测试系统配置忽略非法输入
        SystemConfig config = new SystemConfig();
        config.setFloorCount(-1);
        config.setElevatorCount(0);
        config.setMaxLoad(-10);
        assertEquals(20, config.getFloorCount());
        assertEquals(4, config.getElevatorCount());
        assertEquals(800, config.getMaxLoad(), 0.01);
    }

    // ==================== ElevatorManager测试 ====================

    @Test(timeout = 4000)
    public void testElevatorManagerRegistersAndRetrieves() {
        // 测试电梯管理器能够注册和查询电梯
        ElevatorManager manager = new ElevatorManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        manager.registerElevator(elevator);
        assertSame(elevator, manager.getElevatorById(1));
        assertEquals(1, manager.getAllElevators().size());
    }

    // ==================== Elevator核心逻辑测试 ====================

    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionBranches() {
        // 测试电梯方向更新的三个分支
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.updateDirection();
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());

        elevator.getDestinationSet().add(5);
        elevator.setCurrentFloor(2);
        elevator.updateDirection();
        assertEquals(Direction.UP, elevator.getDirection());

        elevator.setCurrentFloor(8);
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test(timeout = 4000)
    public void testElevatorMoveCompletesDestination() throws Exception {
        // 测试电梯移动到目标层并触发开门逻辑
        List<Elevator> elevators = new ArrayList<>();
        StubScheduler scheduler = new StubScheduler(elevators, 15);
        FastElevator elevator = new FastElevator(1, scheduler);
        elevators.add(elevator);
        elevator.setCurrentFloor(2);
        elevator.getDestinationSet().add(3);
        PassengerRequest req = new PassengerRequest(2, 4, Priority.MEDIUM, RequestType.STANDARD);
        elevator.getPassengerList().add(req);
        elevator.move();
        assertEquals(3, elevator.getCurrentFloor());
        assertFalse(elevator.getDestinationSet().contains(3));
        assertEquals(ElevatorStatus.STOPPED, elevator.getStatus());
    }

    @Test(timeout = 4000)
    public void testElevatorMoveDownAndEnergy() throws Exception {
        // 测试电梯向下移动且能量消耗递增
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 20, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(2, scheduler);
        elevator.setCurrentFloor(10);
        elevator.setDirection(Direction.DOWN);
        elevator.getDestinationSet().add(5);
        double before = elevator.getEnergyConsumption();
        elevator.move();
        assertEquals(9, elevator.getCurrentFloor());
        assertTrue(elevator.getEnergyConsumption() > before);
    }

    @Test(timeout = 4000)
    public void testElevatorLoadPassengersStopsAtMax() {
        // 测试装载乘客时尊重最大载重限制
        List<Elevator> elevators = new ArrayList<>();
        StubScheduler scheduler = new StubScheduler(elevators, 20);
        FastElevator elevator = new FastElevator(1, scheduler);
        elevators.add(elevator);
        elevator.setCurrentFloor(1);
        PassengerRequest r1 = new PassengerRequest(1, 8, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest r2 = new PassengerRequest(1, 9, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.addManualRequests(1, Direction.UP, r1, r2);
        elevator.setCurrentLoad(elevator.getMaxLoad() - 50);
        elevator.loadPassengers();
        assertTrue(elevator.getCurrentLoad() <= elevator.getMaxLoad());
    }

    @Test(timeout = 4000)
    public void testElevatorUnloadPassengersRemovesCurrentFloor() {
        // 测试卸载乘客只移除当前楼层乘客
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 15, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(3, scheduler);
        PassengerRequest stay = new PassengerRequest(1, 7, Priority.LOW, RequestType.STANDARD);
        PassengerRequest leave = new PassengerRequest(1, 5, Priority.LOW, RequestType.STANDARD);
        elevator.getPassengerList().add(stay);
        elevator.getPassengerList().add(leave);
        elevator.setCurrentFloor(5);
        elevator.unloadPassengers();
        assertEquals(1, elevator.getPassengerList().size());
        assertEquals(7, elevator.getPassengerList().get(0).getDestinationFloor());
    }

    @Test(timeout = 4000)
    public void testElevatorHandleEmergencyResetsState() {
        // 测试紧急处理会清空乘客并添加一楼目的地
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(4, scheduler);
        elevator.getPassengerList().add(new PassengerRequest(1, 6, Priority.HIGH, RequestType.STANDARD));
        elevator.getDestinationSet().addAll(Arrays.asList(6, 8));
        elevator.handleEmergency();
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
        assertTrue(elevator.getPassengerList().isEmpty());
        assertEquals(Arrays.asList(1), new ArrayList<>(elevator.getDestinationSet()));
    }

    @Test(timeout = 4000)
    public void testElevatorClearAllRequestsReturnsSnapshot() {
        // 测试清空请求返回的列表与内部状态解耦
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(5, scheduler);
        elevator.getPassengerList().add(new PassengerRequest(1, 9, Priority.MEDIUM, RequestType.STANDARD));
        List<PassengerRequest> snapshot = elevator.clearAllRequests();
        assertEquals(1, snapshot.size());
        assertTrue(elevator.getPassengerList().isEmpty());
    }

    @Test(timeout = 4000)
    public void testElevatorObserverReceivesCustomEvent() {
        // 测试自定义观察者能收到事件对象
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 8, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(6, scheduler);
        RecordingObserver observer = new RecordingObserver();
        elevator.addObserver(observer);
        Event event = new Event(EventType.EMERGENCY, "payload");
        elevator.notifyObservers(event);
        assertSame(event, observer.lastArg);
    }

    // ==================== Scheduler测试 ====================

    @Test(timeout = 4000)
    public void testSchedulerSubmitHighPriorityGoesToQueue() throws Exception {
        // 测试高优先级请求进入独立队列
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 12, new NearestElevatorStrategy());
        PassengerRequest high = new PassengerRequest(2, 5, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(high);
        Field queueField = Scheduler.class.getDeclaredField("highPriorityQueue");
        queueField.setAccessible(true);
        Queue<?> queue = (Queue<?>) queueField.get(scheduler);
        assertEquals(1, queue.size());
    }

    @Test(timeout = 4000)
    public void testSchedulerSubmitNormalStoredOnFloor() {
        // 测试普通优先级请求被保存在对应楼层
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 12, new NearestElevatorStrategy());
        PassengerRequest normal = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(normal);
        List<PassengerRequest> fetched = scheduler.getRequestsAtFloor(3, Direction.UP);
        assertEquals(1, fetched.size());
    }

    @Test(timeout = 4000)
    public void testSchedulerDispatchUsesStrategy() {
        // 测试调度器调用策略并给电梯添加目的地
        List<Elevator> elevators = new ArrayList<>();
        CountingDispatchStrategy strategy = new CountingDispatchStrategy();
        Scheduler scheduler = new Scheduler(elevators, 15, strategy);
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        PassengerRequest request = new PassengerRequest(1, 9, Priority.LOW, RequestType.STANDARD);
        scheduler.dispatchElevator(request);
        assertTrue(elevator.getDestinationSet().contains(1));
        assertEquals(elevator, strategy.getLastElevator());
    }

    @Test(timeout = 4000)
    public void testSchedulerUpdateRoutesEvents() {
        // 测试update方法根据事件类型执行不同逻辑
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy()) {
            boolean redistributed;
            boolean emergency;
            @Override
            public void redistributeRequests(Elevator faultyElevator) {
                redistributed = true;
            }
            @Override
            public void executeEmergencyProtocol() {
                emergency = true;
            }
        };
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        scheduler.update(elevator, new Event(EventType.ELEVATOR_FAULT, null));
        scheduler.update(elevator, new Event(EventType.EMERGENCY, null));
        scheduler.update(elevator, new Event(EventType.CONFIG_UPDATED, null));
        try {
            Field redField = scheduler.getClass().getDeclaredField("redistributed");
            Field emerField = scheduler.getClass().getDeclaredField("emergency");
            redField.setAccessible(true);
            emerField.setAccessible(true);
            assertTrue(redField.getBoolean(scheduler));
            assertTrue(emerField.getBoolean(scheduler));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSchedulerRedistributeMovesRequests() {
        // 测试重新分配会清空故障电梯的请求并重新调度
        List<Elevator> elevators = new ArrayList<>();
        StubScheduler scheduler = new StubScheduler(elevators, 10);
        FastElevator faulty = new FastElevator(1, scheduler);
        elevators.add(faulty);
        faulty.getPassengerList().add(new PassengerRequest(2, 9, Priority.LOW, RequestType.STANDARD));
        scheduler.redistributeRequests(faulty);
        assertTrue(faulty.getPassengerList().isEmpty());
        assertEquals(1, scheduler.getDispatched().size());
    }

    @Test(timeout = 4000)
    public void testSchedulerExecuteEmergencyProtocolInvokesElevators() {
        // 测试紧急协议会遍历所有电梯
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 5, new NearestElevatorStrategy());
        EmergencyAwareElevator e1 = new EmergencyAwareElevator(1, scheduler);
        EmergencyAwareElevator e2 = new EmergencyAwareElevator(2, scheduler);
        elevators.add(e1);
        elevators.add(e2);
        scheduler.executeEmergencyProtocol();
        assertTrue(e1.isHandled());
        assertTrue(e2.isHandled());
    }

    // ==================== 策略类测试 ====================

    @Test(timeout = 4000)
    public void testNearestElevatorStrategySelectsClosest() {
        // 测试最近电梯策略选择距离最近的电梯
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator e1 = new Elevator(1, scheduler);
        e1.setCurrentFloor(2);
        e1.setStatus(ElevatorStatus.IDLE);
        Elevator e2 = new Elevator(2, scheduler);
        e2.setCurrentFloor(8);
        e2.setStatus(ElevatorStatus.IDLE);
        List<Elevator> elevators = Arrays.asList(e1, e2);
        PassengerRequest request = new PassengerRequest(4, 6, Priority.MEDIUM, RequestType.STANDARD);
        assertSame(e1, strategy.selectElevator(elevators, request));
    }

    @Test(timeout = 4000)
    public void testNearestElevatorStrategyEligibilityBranches() {
        // 测试最近策略的资格判断
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 5, strategy);
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        PassengerRequest request = new PassengerRequest(1, 3, Priority.LOW, RequestType.STANDARD);
        assertTrue(strategy.isEligible(elevator, request));
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.UP);
        assertTrue(strategy.isEligible(elevator, request));
        elevator.setDirection(Direction.DOWN);
        assertFalse(strategy.isEligible(elevator, request));
        elevator.setStatus(ElevatorStatus.MAINTENANCE);
        assertFalse(strategy.isEligible(elevator, request));
    }

    @Test(timeout = 4000)
    public void testHighEfficiencyStrategyIsCloser() {
        // 测试高效策略通过距离比较挑选电梯
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 8, strategy);
        Elevator close = new Elevator(1, scheduler);
        close.setCurrentFloor(3);
        close.setStatus(ElevatorStatus.IDLE);
        Elevator far = new Elevator(2, scheduler);
        far.setCurrentFloor(7);
        far.setStatus(ElevatorStatus.MOVING);
        far.setDirection(Direction.UP);
        PassengerRequest request = new PassengerRequest(4, 9, Priority.MEDIUM, RequestType.STANDARD);
        Elevator selected = strategy.selectElevator(Arrays.asList(far, close), request);
        assertSame(close, selected);
        assertTrue(strategy.isCloser(close, far, request));
        assertFalse(strategy.isCloser(far, close, request));
    }

    @Test(timeout = 4000)
    public void testEnergySavingStrategyHandlesFallback() {
        // 测试节能策略优先空闲电梯，其次同向且距离小于5
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator idle = new Elevator(1, scheduler);
        idle.setStatus(ElevatorStatus.IDLE);
        Elevator movingNear = new Elevator(2, scheduler);
        movingNear.setStatus(ElevatorStatus.MOVING);
        movingNear.setDirection(Direction.UP);
        movingNear.setCurrentFloor(3);
        List<Elevator> elevators = Arrays.asList(movingNear, idle);
        PassengerRequest request = new PassengerRequest(2, 9, Priority.LOW, RequestType.STANDARD);
        assertSame(idle, strategy.selectElevator(elevators, request));
        idle.setStatus(ElevatorStatus.MOVING);
        assertSame(movingNear, strategy.selectElevator(elevators, request));
        movingNear.setCurrentFloor(10);
        assertNull(strategy.selectElevator(elevators, request));
    }

    @Test(timeout = 4000)
    public void testPredictiveSchedulingStrategyPrefersLowerCost() {
        // 测试预测策略选择计算代价最低的电梯
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 12, strategy);
        Elevator near = new Elevator(1, scheduler);
        near.setCurrentFloor(2);
        Elevator far = new Elevator(2, scheduler);
        far.setCurrentFloor(9);
        List<Elevator> elevators = Arrays.asList(far, near);
        PassengerRequest request = new PassengerRequest(3, 8, Priority.MEDIUM, RequestType.STANDARD);
        assertSame(near, strategy.selectElevator(elevators, request));
        double cost = strategy.calculatePredictedCost(near, request);
        assertTrue(cost >= 0);
    }

    // ==================== Event与EventBus测试 ====================

    @Test(timeout = 4000)
    public void testEventHoldsTypeAndData() {
        // 测试事件对象保存类型和数据
        Event event = new Event(EventType.MAINTENANCE_REQUIRED, "data");
        assertEquals(EventType.MAINTENANCE_REQUIRED, event.getType());
        assertEquals("data", event.getData());
    }

    @Test(timeout = 4000)
    public void testEventBusSubscribeAndPublish() {
        // 测试事件总线的订阅与发布
        EventBus bus = new EventBus();
        final List<EventBus.Event> received = new ArrayList<>();
        bus.subscribe(EventType.EMERGENCY, received::add);
        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, "fire");
        bus.publish(event);
        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    // ==================== NotificationService测试 ====================

    @Test(timeout = 4000)
    public void testNotificationServiceChannelFiltering() throws Exception {
        // 测试通知服务根据渠道支持类型发送
        NotificationService service = new NotificationService();
        Field channelsField = NotificationService.class.getDeclaredField("channels");
        channelsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<NotificationService.NotificationChannel> channels = (List<NotificationService.NotificationChannel>) channelsField.get(service);
        channels.clear();
        RecordingChannel sms = new RecordingChannel(NotificationService.NotificationType.EMERGENCY);
        RecordingChannel email = new RecordingChannel(NotificationService.NotificationType.values());
        channels.add(sms);
        channels.add(email);
        NotificationService.Notification notification = new NotificationService.Notification(
                NotificationService.NotificationType.EMERGENCY,
                "test", Collections.singletonList("user@a.com"));
        service.sendNotification(notification);
        assertEquals(1, sms.getReceived().size());
        assertEquals(1, email.getReceived().size());
    }

    // ==================== AnalyticsEngine测试 ====================

    @Test(timeout = 4000)
    public void testAnalyticsEnginePeakHoursCalculation() throws Exception {
        // 测试分析引擎根据人数判断是否高峰期
        AnalyticsEngine engine = new AnalyticsEngine();
        engine.updateFloorPassengerCount(1, 20);
        engine.updateFloorPassengerCount(2, 31);
        assertTrue(engine.isPeakHours());
        engine.updateFloorPassengerCount(2, 30);
        assertFalse(engine.isPeakHours());
        ElevatorStatusReport report = new ElevatorStatusReport(1, 5, Direction.UP, ElevatorStatus.MOVING, 1.2, 600, 8);
        engine.processStatusReport(report);
        Field reportsField = AnalyticsEngine.class.getDeclaredField("statusReports");
        reportsField.setAccessible(true);
        List<?> reports = (List<?>) reportsField.get(engine);
        assertEquals(1, reports.size());
    }

    @Test(timeout = 4000)
    public void testAnalyticsEngineGeneratesReport() {
        // 测试生成的统计报告包含标题和时间
        AnalyticsEngine engine = new AnalyticsEngine();
        AnalyticsEngine.Report report = engine.generatePerformanceReport();
        assertEquals("System Performance Report", report.getTitle());
        assertTrue(report.getGeneratedTime() > 0);
    }

    // ==================== LogManager测试 ====================

    @Test(timeout = 4000)
    public void testLogManagerRecordAndQuery() {
        // 测试日志记录以及按时间和来源查询
        LogManager manager = new LogManager();
        manager.recordEvent("Scheduler", "start");
        manager.recordEvent("Elevator", "move");
        long now = System.currentTimeMillis();
        List<LogManager.SystemLog> logs = manager.queryLogs("Elevator", now - 1000, now + 1000);
        assertEquals(1, logs.size());
        assertEquals("move", logs.get(0).getMessage());
    }

    // ==================== MaintenanceManager测试 ====================

    @Test(timeout = 4000)
    public void testMaintenanceManagerSchedulesTasks() throws Exception {
        // 测试维护管理器能够根据事件创建任务
        MaintenanceManager manager = new MaintenanceManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 5, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        manager.onEvent(new EventBus.Event(EventType.ELEVATOR_FAULT, elevator));
        Field queueField = MaintenanceManager.class.getDeclaredField("taskQueue");
        queueField.setAccessible(true);
        Queue<?> queue = (Queue<?>) queueField.get(manager);
        assertFalse(queue.isEmpty());
        shutdownExecutorField(manager, "executorService");
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerRecordsResults() throws Exception {
        // 测试维护记录的写入
        MaintenanceManager manager = new MaintenanceManager();
        manager.recordMaintenanceResult(1, "done");
        Field recordsField = MaintenanceManager.class.getDeclaredField("maintenanceRecords");
        recordsField.setAccessible(true);
        List<?> records = (List<?>) recordsField.get(manager);
        assertEquals(1, records.size());
        shutdownExecutorField(manager, "executorService");
    }

    // ==================== SecurityMonitor测试 ====================

    @Test(timeout = 4000)
    public void testSecurityMonitorHandleEmergency() throws Exception {
        // 测试安全监控触发通知并执行调度紧急协议
        StubNotificationService notificationService = new StubNotificationService();
        EmergencyScheduler emergencyScheduler = new EmergencyScheduler();
        setSingleton(NotificationService.class, notificationService);
        setSingleton(Scheduler.class, emergencyScheduler);
        SecurityMonitor monitor = new SecurityMonitor();
        monitor.handleEmergency("fire");
        Field eventsField = SecurityMonitor.class.getDeclaredField("securityEvents");
        eventsField.setAccessible(true);
        List<?> events = (List<?>) eventsField.get(monitor);
        assertEquals(1, events.size());
        assertEquals(1, notificationService.getSent().size());
        assertTrue(emergencyScheduler.isEmergencyTriggered());
        shutdownExecutorField(monitor, "executorService");
    }

    private void setSingleton(Class<?> clazz, Object instance) throws Exception {
        Field field = clazz.getDeclaredField("instance");
        field.setAccessible(true);
        // remove final modifier if necessary
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, instance);
    }

    // ==================== ThreadPoolManager测试 ====================

    @Test(timeout = 4000)
    public void testThreadPoolManagerRunsTasks() throws Exception {
        // 测试线程池能执行提交的任务
        ThreadPoolManager manager = new ThreadPoolManager();
        CountDownLatch latch = new CountDownLatch(1);
        manager.submitTask(latch::countDown);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        manager.shutdown();
    }

    // ==================== ElevatorStatusReport与日志实体测试 ====================

    @Test(timeout = 4000)
    public void testElevatorStatusReportFieldsAndToString() {
        // 测试电梯状态报告的字段与字符串输出
        ElevatorStatusReport report = new ElevatorStatusReport(2, 7, Direction.DOWN, ElevatorStatus.MOVING, 1.8, 650, 9);
        assertEquals(2, report.getElevatorId());
        assertTrue(report.toString().contains("elevatorId=2"));
    }

    @Test(timeout = 4000)
    public void testSystemLogHoldsValues() {
        // 测试系统日志实体保存字段
        LogManager.SystemLog log = new LogManager.SystemLog("src", "msg", 123L);
        assertEquals("src", log.getSource());
        assertEquals("msg", log.getMessage());
        assertEquals(123L, log.getTimestamp());
    }
}
