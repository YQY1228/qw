package net.mooctest;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * 所有业务测试集中在一个文件中，便于统一管理和提升整体覆盖率。
 */
public class ElevatorManagerTest {

    @Before
    public void resetEnvironment() throws Exception {
        // 中文注释：测试前清理各个单例，确保每个测试在独立干净的环境中运行
        resetSingleton(AnalyticsEngine.class);
        resetSingleton(ElevatorManager.class);
        resetSingleton(EventBus.class);
        resetSingleton(LogManager.class);
        resetSingleton(NotificationService.class);
        resetSingleton(Scheduler.class);
        resetSingleton(SecurityMonitor.class);
        resetSingleton(SystemConfig.class);
        resetSingleton(ThreadPoolManager.class);
        resetSingleton(MaintenanceManager.class);

        // 重建系统配置，确保默认数值一致
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(30);
        config.setElevatorCount(6);
        config.setMaxLoad(700);
    }

    private void resetSingleton(Class<?> clazz) throws Exception {
        try {
            Field field = clazz.getDeclaredField("instance");
            field.setAccessible(true);
            Object inst = field.get(null);
            if (inst != null) {
                try {
                    Field executorField = inst.getClass().getDeclaredField("executorService");
                    executorField.setAccessible(true);
                    Object executor = executorField.get(inst);
                    if (executor instanceof ExecutorService) {
                        ((ExecutorService) executor).shutdownNow();
                    }
                } catch (NoSuchFieldException ignoredInner) {
                    // 对没有执行器的单例无需处理
                }
            }
            field.set(null, null);
        } catch (NoSuchFieldException ignored) {
            // 无该字段的类不需要处理
        }
    }

    private Elevator createElevator(int id, int floor) {
        Scheduler schedulerMock = Mockito.mock(Scheduler.class);
        Elevator elevator = new Elevator(id, schedulerMock);
        elevator.setCurrentFloor(floor);
        elevator.setStatus(ElevatorStatus.IDLE);
        elevator.setDirection(Direction.UP);
        elevator.setMode(ElevatorMode.NORMAL);
        return elevator;
    }

    private void setPassengerCount(Elevator elevator, int count) throws Exception {
        Field passengerField = Elevator.class.getDeclaredField("passengerList");
        passengerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> passengerList = (List<PassengerRequest>) passengerField.get(elevator);
        passengerList.clear();
        for (int i = 0; i < count; i++) {
            passengerList.add(new PassengerRequest(1, 2, Priority.LOW, RequestType.STANDARD));
        }
        elevator.setCurrentLoad(count * 70.0);
    }

    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        T value = (T) field.get(target);
        return value;
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void awaitCondition(BooleanSupplier condition, long timeoutMillis, String failureMessage) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(20L);
        }
        fail(failureMessage);
    }

    @Test
    public void testSystemConfigValidationAndDefaults() {
        // 中文注释：验证系统配置的默认值以及非法输入保护逻辑
        SystemConfig config = SystemConfig.getInstance();
        assertEquals(30, config.getFloorCount());
        assertEquals(6, config.getElevatorCount());
        assertEquals(700, config.getMaxLoad(), 0.0001);

        config.setFloorCount(40);
        config.setElevatorCount(8);
        config.setMaxLoad(900);
        assertEquals(40, config.getFloorCount());
        assertEquals(8, config.getElevatorCount());
        assertEquals(900, config.getMaxLoad(), 0.0001);

        config.setFloorCount(-1);
        config.setElevatorCount(0);
        config.setMaxLoad(-5);
        assertEquals("非法值不应改变已存在的配置", 40, config.getFloorCount());
        assertEquals(8, config.getElevatorCount());
        assertEquals(900, config.getMaxLoad(), 0.0001);
    }

    @Test
    public void testElevatorManagerSingletonAndRegistry() {
        // 中文注释：验证单例与注册查询逻辑，确保电梯映射稳定
        ElevatorManager manager = ElevatorManager.getInstance();
        ElevatorManager again = ElevatorManager.getInstance();
        assertSame(manager, again);

        Elevator elevator = createElevator(1, 3);
        manager.registerElevator(elevator);
        assertSame(elevator, manager.getElevatorById(1));

        Collection<Elevator> elevators = manager.getAllElevators();
        assertTrue(elevators.contains(elevator));
    }

    @Test
    public void testElevatorManagerRegisterDuplicateOverrides() {
        // 中文注释：验证重复注册会覆盖旧电梯实例
        ElevatorManager manager = ElevatorManager.getInstance();
        Elevator first = createElevator(200, 2);
        Elevator second = createElevator(200, 9);
        manager.registerElevator(first);
        manager.registerElevator(second);
        assertSame(second, manager.getElevatorById(200));
    }

    @Test
    public void testElevatorManagerCollectionReflectsRegistrations() {
        // 中文注释：验证getAllElevators返回的集合会实时反映新增电梯
        ElevatorManager manager = ElevatorManager.getInstance();
        Collection<Elevator> collection = manager.getAllElevators();
        Elevator e1 = createElevator(201, 4);
        Elevator e2 = createElevator(202, 5);
        manager.registerElevator(e1);
        assertTrue(collection.contains(e1));
        manager.registerElevator(e2);
        assertTrue(collection.contains(e2));
    }

    @Test
    public void testElevatorManagerConcurrentGetInstance() throws Exception {
        // 中文注释：多线程并发获取单例也应保持同一实例
        Field instanceField = ElevatorManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        List<ElevatorManager> instances = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    instances.add(ElevatorManager.getInstance());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        startLatch.countDown();
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        ElevatorManager expected = instances.get(0);
        for (ElevatorManager manager : instances) {
            assertSame(expected, manager);
        }
        instanceField.set(null, null);
    }

    @Test
    public void testAnalyticsEnginePeakDetectionAndReport() throws Exception {
        // 中文注释：验证统计引擎记录报表与高峰判断逻辑
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        ElevatorStatusReport report = new ElevatorStatusReport(1, 5, Direction.UP, ElevatorStatus.MOVING, 2.5, 140, 3);
        engine.processStatusReport(report);

        List<ElevatorStatusReport> reports = getPrivateField(engine, "statusReports");
        assertEquals(1, reports.size());
        assertSame(report, reports.get(0));

        engine.updateFloorPassengerCount(1, 30);
        engine.updateFloorPassengerCount(2, 25);
        assertTrue("总人数超过阈值即为高峰", engine.isPeakHours());

        AnalyticsEngine.Report generated = engine.generatePerformanceReport();
        assertEquals("System Performance Report", generated.getTitle());
        assertTrue(generated.getGeneratedTime() > 0);
    }

    @Test
    public void testAnalyticsEnginePassengerCountUpdatesOverride() {
        // 中文注释：验证同一楼层的乘客统计会被最新数据覆盖
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        engine.updateFloorPassengerCount(3, 10);
        engine.updateFloorPassengerCount(3, 5);
        assertFalse("少量等待乘客不应触发高峰", engine.isPeakHours());
        engine.updateFloorPassengerCount(4, 60);
        assertTrue("单层大量乘客应立即触发高峰", engine.isPeakHours());
    }

    @Test
    public void testCoreEventValueObject() {
        // 中文注释：验证业务事件对象携带类型与数据的能力
        Event event = new Event(EventType.CONFIG_UPDATED, "payload");
        assertEquals(EventType.CONFIG_UPDATED, event.getType());
        assertEquals("payload", event.getData());
    }

    @Test
    public void testEventBusSubscribeAndPublish() {
        // 中文注释：验证事件总线可以订阅与派发，并且对未订阅事件保持静默
        EventBus bus = EventBus.getInstance();
        AtomicReference<EventBus.Event> captured = new AtomicReference<>();
        bus.subscribe(EventType.EMERGENCY, captured::set);

        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, "fire");
        bus.publish(event);
        assertSame(event, captured.get());

        // 发布无人订阅的事件以覆盖空分支
        bus.publish(new EventBus.Event(EventType.MAINTENANCE_REQUIRED, "noop"));
        assertSame(event, captured.get());
    }

    @Test
    public void testEventBusNotifiesAllListeners() {
        // 中文注释：验证同一事件的多个监听器都会收到通知
        EventBus bus = EventBus.getInstance();
        AtomicInteger counter = new AtomicInteger();
        bus.subscribe(EventType.ELEVATOR_FAULT, event -> counter.addAndGet(1));
        bus.subscribe(EventType.ELEVATOR_FAULT, event -> counter.addAndGet(2));
        bus.publish(new EventBus.Event(EventType.ELEVATOR_FAULT, "fault"));
        assertEquals(3, counter.get());
    }

    @Test
    public void testEventBusEventValueObject() {
        // 中文注释：验证事件总线内部事件对象的取值逻辑
        EventBus.Event event = new EventBus.Event(EventType.MAINTENANCE_REQUIRED, "data");
        assertEquals(EventType.MAINTENANCE_REQUIRED, event.getType());
        assertEquals("data", event.getData());
    }

    @Test
    public void testFloorQueuesPerDirection() {
        // 中文注释：验证楼层请求队列按照方向隔离，并在读取后清空
        Floor floor = new Floor(5);
        PassengerRequest up = new PassengerRequest(5, 8, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest down = new PassengerRequest(5, 2, Priority.LOW, RequestType.STANDARD);

        floor.addRequest(up);
        floor.addRequest(down);

        List<PassengerRequest> upList = floor.getRequests(Direction.UP);
        assertEquals(1, upList.size());
        assertSame(up, upList.get(0));
        assertTrue(floor.getRequests(Direction.UP).isEmpty());

        List<PassengerRequest> downList = floor.getRequests(Direction.DOWN);
        assertEquals(1, downList.size());
        assertSame(down, downList.get(0));
    }

    @Test
    public void testFloorGetFloorNumberAndEmptyQueues() {
        // 中文注释：验证楼层编号读取和空队列返回
        Floor floor = new Floor(12);
        assertEquals(12, floor.getFloorNumber());
        assertTrue(floor.getRequests(Direction.UP).isEmpty());
        assertTrue(floor.getRequests(Direction.DOWN).isEmpty());
    }

    @Test
    public void testLogManagerQueryWithTimeWindow() {
        // 中文注释：验证日志管理器能够按来源和时间范围筛选
        LogManager manager = LogManager.getInstance();
        long start = System.currentTimeMillis();
        manager.recordSchedulerEvent("dispatch");
        manager.recordElevatorEvent(2, "arrived");
        manager.recordEvent("SecurityMonitor", "alert");
        long end = System.currentTimeMillis();

        List<LogManager.SystemLog> schedulerLogs = manager.queryLogs("Scheduler", start, end);
        assertEquals(1, schedulerLogs.size());
        assertEquals("dispatch", schedulerLogs.get(0).getMessage());

        LogManager.SystemLog custom = new LogManager.SystemLog("source", "msg", 100L);
        assertEquals("source", custom.getSource());
        assertEquals("msg", custom.getMessage());
        assertEquals(100L, custom.getTimestamp());
    }

    @Test
    public void testLogManagerQueryOutsideWindowReturnsEmpty() {
        // 中文注释：验证时间窗口与来源不匹配时返回空集合
        LogManager manager = LogManager.getInstance();
        String source = "Coverage" + System.nanoTime();
        manager.recordEvent(source, "message");
        long futureStart = System.currentTimeMillis() + 1000;
        long futureEnd = futureStart + 1000;
        List<LogManager.SystemLog> logs = manager.queryLogs(source, futureStart, futureEnd);
        assertTrue(logs.isEmpty());
    }

    @Test
    public void testLogManagerQueryBoundaryInclusive() throws Exception {
        // 中文注释：验证时间边界是闭区间
        LogManager manager = LogManager.getInstance();
        @SuppressWarnings("unchecked")
        List<LogManager.SystemLog> logs = getPrivateField(manager, "logs");
        long timestamp = System.currentTimeMillis();
        logs.add(new LogManager.SystemLog("BoundarySource", "hit", timestamp));
        List<LogManager.SystemLog> result = manager.queryLogs("BoundarySource", timestamp, timestamp);
        assertEquals(1, result.size());
        assertEquals("hit", result.get(0).getMessage());
    }

    @Test
    public void testNotificationServiceCustomChannel() throws Exception {
        // 中文注释：通过注入自定义通道验证通知路由逻辑
        NotificationService service = NotificationService.getInstance();
        @SuppressWarnings("unchecked")
        List<NotificationService.NotificationChannel> channels = getPrivateField(service, "channels");
        AtomicReference<NotificationService.Notification> captured = new AtomicReference<>();
        NotificationService.NotificationChannel customChannel = new NotificationService.NotificationChannel() {
            @Override
            public boolean supports(NotificationService.NotificationType type) {
                return type == NotificationService.NotificationType.INFORMATION;
            }

            @Override
            public void send(NotificationService.Notification notification) {
                captured.set(notification);
            }
        };
        channels.add(customChannel);

        NotificationService.Notification notification = new NotificationService.Notification(
                NotificationService.NotificationType.INFORMATION,
                "系统升级",
                Arrays.asList("ops@company.com"));
        service.sendNotification(notification);
        assertSame(notification, captured.get());
        assertEquals("系统升级", captured.get().getMessage());
        assertEquals(Arrays.asList("ops@company.com"), captured.get().getRecipients());
    }

    @Test
    public void testNotificationServiceDefaultChannelsSupportMatrix() throws Exception {
        // 中文注释：验证默认通道的支持矩阵以及只向支持的通道发送
        NotificationService.SMSChannel smsChannel = new NotificationService.SMSChannel();
        assertTrue(smsChannel.supports(NotificationService.NotificationType.EMERGENCY));
        assertTrue(smsChannel.supports(NotificationService.NotificationType.MAINTENANCE));
        assertFalse(smsChannel.supports(NotificationService.NotificationType.INFORMATION));

        NotificationService.EmailChannel emailChannel = new NotificationService.EmailChannel();
        for (NotificationService.NotificationType type : NotificationService.NotificationType.values()) {
            assertTrue(emailChannel.supports(type));
        }

        NotificationService service = NotificationService.getInstance();
        @SuppressWarnings("unchecked")
        List<NotificationService.NotificationChannel> channels = getPrivateField(service, "channels");
        List<NotificationService.NotificationChannel> backup = new ArrayList<>(channels);
        AtomicReference<NotificationService.Notification> captured = new AtomicReference<>();
        NotificationService.NotificationChannel denyChannel = new NotificationService.NotificationChannel() {
            @Override
            public boolean supports(NotificationService.NotificationType type) {
                return false;
            }

            @Override
            public void send(NotificationService.Notification notification) {
                fail("不支持的通道不应被调用");
            }
        };
        NotificationService.NotificationChannel acceptChannel = new NotificationService.NotificationChannel() {
            @Override
            public boolean supports(NotificationService.NotificationType type) {
                return true;
            }

            @Override
            public void send(NotificationService.Notification notification) {
                captured.set(notification);
            }
        };
        channels.clear();
        channels.add(denyChannel);
        channels.add(acceptChannel);
        try {
            NotificationService.Notification emergency = new NotificationService.Notification(
                    NotificationService.NotificationType.EMERGENCY,
                    "紧急广播",
                    Arrays.asList("duty@building.com"));
            service.sendNotification(emergency);
            assertSame(emergency, captured.get());
        } finally {
            channels.clear();
            channels.addAll(backup);
        }
    }

    @Test
    public void testNotificationValueObjectAccessors() {
        // 中文注释：验证通知实体的字段访问逻辑
        List<String> recipients = Arrays.asList("ops@corp.com", "it@corp.com");
        NotificationService.Notification notification = new NotificationService.Notification(
                NotificationService.NotificationType.SYSTEM_UPDATE,
                "系统更新",
                recipients);
        assertEquals(NotificationService.NotificationType.SYSTEM_UPDATE, notification.getType());
        assertEquals("系统更新", notification.getMessage());
        assertSame(recipients, notification.getRecipients());
    }

    @Test
    public void testPassengerRequestDerivedValues() {
        // 中文注释：验证乘客请求自动推导方向与字符串格式
        PassengerRequest up = new PassengerRequest(1, 5, Priority.HIGH, RequestType.DESTINATION_CONTROL);
        assertEquals(Direction.UP, up.getDirection());
        assertEquals(Priority.HIGH, up.getPriority());
        assertEquals(RequestType.DESTINATION_CONTROL, up.getRequestType());
        assertTrue(up.getTimestamp() > 0);
        assertTrue(up.toString().contains("From 1 to 5"));

        PassengerRequest down = new PassengerRequest(6, 2, Priority.LOW, RequestType.STANDARD);
        assertEquals(Direction.DOWN, down.getDirection());
        assertEquals(SpecialNeeds.NONE, down.getSpecialNeeds());
    }

    @Test
    public void testPassengerRequestSameFloorDefaultsDownDirection() throws InterruptedException {
        // 中文注释：验证起止楼层相同的边界情况默认视为向下请求
        PassengerRequest stay = new PassengerRequest(4, 4, Priority.MEDIUM, RequestType.STANDARD);
        assertEquals(Direction.DOWN, stay.getDirection());
        long firstTimestamp = stay.getTimestamp();
        Thread.sleep(2L);
        PassengerRequest later = new PassengerRequest(4, 4, Priority.MEDIUM, RequestType.STANDARD);
        assertTrue("时间戳应保持单调递增", later.getTimestamp() >= firstTimestamp);
    }

    @Test
    public void testPredictiveStrategySelectsLowestCost() throws Exception {
        // 中文注释：验证预测调度策略能够挑选成本最低的电梯
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Elevator e1 = createElevator(1, 2);
        Elevator e2 = createElevator(2, 10);
        setPassengerCount(e2, 5);

        PassengerRequest request = new PassengerRequest(5, 12, Priority.MEDIUM, RequestType.STANDARD);
        List<Elevator> elevators = Arrays.asList(e1, e2);
        Elevator selected = strategy.selectElevator(elevators, request);
        assertSame("距离更近的电梯应该被选中", e1, selected);

        double cost = strategy.calculatePredictedCost(e2, request);
        assertTrue("成本计算应包含距离和负载", cost > Math.abs(e2.getCurrentFloor() - request.getStartFloor()));
    }

    @Test
    public void testPredictiveStrategyCostFormula() throws Exception {
        // 中文注释：验证预测成本计算严格遵循距离+负载因子
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Elevator elevator = createElevator(7, 2);
        setPassengerCount(elevator, 7);
        PassengerRequest request = new PassengerRequest(6, 9, Priority.MEDIUM, RequestType.STANDARD);
        double expected = Math.abs(elevator.getCurrentFloor() - request.getStartFloor())
                + (elevator.getPassengerList().size() / elevator.getMaxLoad()) * 10;
        assertEquals(expected, strategy.calculatePredictedCost(elevator, request), 0.0001);
    }

    @Test
    public void testNearestStrategyEligibilityAndDistance() {
        // 中文注释：验证最近电梯策略的资格判断与距离比较
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        PassengerRequest request = new PassengerRequest(3, 8, Priority.LOW, RequestType.STANDARD);

        Elevator idle = createElevator(1, 5);
        Elevator movingSame = createElevator(2, 1);
        movingSame.setStatus(ElevatorStatus.MOVING);
        movingSame.setDirection(Direction.UP);
        Elevator movingOpposite = createElevator(3, 7);
        movingOpposite.setStatus(ElevatorStatus.MOVING);
        movingOpposite.setDirection(Direction.DOWN);

        List<Elevator> list = Arrays.asList(idle, movingSame, movingOpposite);
        Elevator result = strategy.selectElevator(list, request);
        assertSame(idle, result);
        assertTrue(strategy.isEligible(idle, request));
        assertTrue(strategy.isEligible(movingSame, request));
        assertFalse(strategy.isEligible(movingOpposite, request));
    }

    @Test
    public void testNearestStrategyPrefersFirstWhenDistanceTies() {
        // 中文注释：验证当距离相等时不会更换已选电梯
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        PassengerRequest request = new PassengerRequest(4, 10, Priority.MEDIUM, RequestType.STANDARD);
        Elevator first = createElevator(10, 2);
        Elevator second = createElevator(11, 6);
        first.setStatus(ElevatorStatus.IDLE);
        second.setStatus(ElevatorStatus.MOVING);
        second.setDirection(Direction.UP);
        List<Elevator> elevators = Arrays.asList(first, second);
        Elevator chosen = strategy.selectElevator(elevators, request);
        assertSame("距离相等时应保留先选的电梯", first, chosen);
    }

    @Test
    public void testEnergySavingStrategyBranches() {
        // 中文注释：验证节能策略优先空闲，其次同向靠近，最后返回空
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Elevator idle = createElevator(1, 2);
        Elevator moving = createElevator(2, 7);
        moving.setDirection(Direction.UP);
        moving.setStatus(ElevatorStatus.MOVING);

        PassengerRequest request = new PassengerRequest(5, 9, Priority.MEDIUM, RequestType.STANDARD);
        Elevator result = strategy.selectElevator(Arrays.asList(idle, moving), request);
        assertSame(idle, result);

        idle.setStatus(ElevatorStatus.MOVING);
        Elevator fallback = strategy.selectElevator(Arrays.asList(idle, moving), request);
        assertSame(moving, fallback);

        moving.setDirection(Direction.DOWN);
        moving.setCurrentFloor(20);
        assertNull(strategy.selectElevator(Arrays.asList(idle, moving), request));
    }

    @Test
    public void testEnergySavingStrategyReturnsNullWhenNoConditionsMatch() {
        // 中文注释：验证两个循环均不满足时确实返回null
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Elevator first = createElevator(1, 12);
        Elevator second = createElevator(2, 20);
        first.setStatus(ElevatorStatus.MOVING);
        second.setStatus(ElevatorStatus.MOVING);
        first.setDirection(Direction.DOWN);
        second.setDirection(Direction.UP);
        PassengerRequest request = new PassengerRequest(5, 1, Priority.LOW, RequestType.STANDARD);
        assertNull(strategy.selectElevator(Arrays.asList(first, second), request));
    }

    @Test
    public void testHighEfficiencyStrategyComparison() {
        // 中文注释：验证高效策略在等距情况下不会误判，并可以正确换乘
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Elevator candidate = createElevator(1, 4);
        Elevator current = createElevator(2, 6);
        PassengerRequest request = new PassengerRequest(5, 9, Priority.MEDIUM, RequestType.STANDARD);

        assertTrue(strategy.isCloser(candidate, current, request));
        candidate.setCurrentFloor(7);
        assertFalse(strategy.isCloser(candidate, current, request));

        Elevator e3 = createElevator(3, 2);
        e3.setStatus(ElevatorStatus.MOVING);
        e3.setDirection(Direction.UP);
        Elevator selected = strategy.selectElevator(Arrays.asList(candidate, current, e3), request);
        assertNotNull(selected);
        assertEquals(3, selected.getId());
    }

    @Test
    public void testSchedulerSubmitQueuesAndDispatch() throws Exception {
        // 中文注释：验证调度器提交请求时，高优先级入队，普通请求进入楼层队列
        Elevator target = createElevator(1, 1);
        List<Elevator> elevators = new ArrayList<>();
        elevators.add(target);
        AtomicReference<PassengerRequest> recorded = new AtomicReference<>();
        DispatchStrategy strategy = (list, request) -> {
            recorded.set(request);
            return target;
        };

        Scheduler scheduler = new Scheduler(elevators, 10, strategy);
        PassengerRequest high = new PassengerRequest(2, 8, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(high);

        Queue<PassengerRequest> highQueue = getPrivateField(scheduler, "highPriorityQueue");
        assertEquals(1, highQueue.size());
        assertSame(high, highQueue.peek());
        assertSame(high, recorded.get());

        PassengerRequest low = new PassengerRequest(3, 1, Priority.LOW, RequestType.STANDARD);
        scheduler.submitRequest(low);
        List<PassengerRequest> downList = scheduler.getRequestsAtFloor(3, Direction.DOWN);
        assertEquals(1, downList.size());
        assertSame(low, downList.get(0));
        assertTrue(scheduler.getRequestsAtFloor(3, Direction.DOWN).isEmpty());
    }

    @Test
    public void testSchedulerGetRequestsAtFloorUpDirection() throws Exception {
        // 中文注释：验证向上请求能够正确排队并被取出
        Elevator elevator = createElevator(12, 1);
        Scheduler scheduler = new Scheduler(Collections.singletonList(elevator), 10, (list, request) -> list.get(0));
        PassengerRequest upRequest = new PassengerRequest(4, 9, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(upRequest);
        List<PassengerRequest> upList = scheduler.getRequestsAtFloor(4, Direction.UP);
        assertEquals(1, upList.size());
        assertSame(upRequest, upList.get(0));
        assertTrue(scheduler.getRequestsAtFloor(4, Direction.UP).isEmpty());
    }

    @Test
    public void testSchedulerDispatchElevatorWhenStrategyReturnsNull() {
        // 中文注释：验证策略返回null时不会向任何电梯添加目的地
        Elevator spyElevator = Mockito.spy(createElevator(8, 1));
        Scheduler scheduler = new Scheduler(Collections.singletonList(spyElevator), 5, (list, request) -> null);
        PassengerRequest request = new PassengerRequest(2, 9, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.dispatchElevator(request);
        Mockito.verify(spyElevator, Mockito.never()).addDestination(Mockito.anyInt());
    }

    @Test
    public void testSchedulerRedistributeRequestsResubmitsAll() throws Exception {
        // 中文注释：验证故障电梯的请求会被完整再分配
        Elevator managed = createElevator(101, 1);
        List<PassengerRequest> dispatched = new ArrayList<>();
        DispatchStrategy strategy = (list, request) -> {
            dispatched.add(request);
            return list.get(0);
        };
        Scheduler scheduler = new Scheduler(Collections.singletonList(managed), 4, strategy);
        Elevator faulty = Mockito.mock(Elevator.class);
        PassengerRequest r1 = new PassengerRequest(2, 6, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest r2 = new PassengerRequest(3, 7, Priority.HIGH, RequestType.DESTINATION_CONTROL);
        Mockito.when(faulty.clearAllRequests()).thenReturn(Arrays.asList(r1, r2));

        scheduler.redistributeRequests(faulty);
        assertEquals(Arrays.asList(r1, r2), dispatched);
        Mockito.verify(faulty).clearAllRequests();
    }

    @Test
    public void testSchedulerUpdateHandlesFaultAndEmergency() throws Exception {
        // 中文注释：验证调度器的事件处理能够分发故障请求并触发应急协议
        Elevator dispatched = createElevator(2, 4);
        List<Elevator> elevators = new ArrayList<>();
        elevators.add(dispatched);
        AtomicInteger dispatchCount = new AtomicInteger();
        DispatchStrategy strategy = (list, request) -> {
            dispatchCount.incrementAndGet();
            return dispatched;
        };
        Scheduler scheduler = new Scheduler(elevators, 5, strategy);

        Elevator faulty = Mockito.spy(createElevator(99, 10));
        PassengerRequest r1 = new PassengerRequest(1, 5, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest r2 = new PassengerRequest(2, 6, Priority.LOW, RequestType.STANDARD);
        Mockito.doReturn(Arrays.asList(r1, r2)).when(faulty).clearAllRequests();

        scheduler.update(faulty, new Event(EventType.ELEVATOR_FAULT, null));
        assertEquals(2, dispatchCount.get());

        Elevator emergencyElevator = Mockito.spy(createElevator(3, 7));
        elevators.add(emergencyElevator);
        scheduler.update(emergencyElevator, new Event(EventType.EMERGENCY, null));
        Mockito.verify(emergencyElevator, Mockito.times(1)).handleEmergency();
    }

    @Test
    public void testSchedulerUpdateIgnoresOtherEvents() {
        // 中文注释：验证非故障和紧急事件不会触发重新调度
        Elevator elevator = Mockito.spy(createElevator(6, 2));
        List<Elevator> elevators = Collections.singletonList(elevator);
        AtomicInteger dispatchCount = new AtomicInteger();
        DispatchStrategy strategy = (list, request) -> {
            dispatchCount.incrementAndGet();
            return list.get(0);
        };
        Scheduler scheduler = new Scheduler(elevators, 3, strategy);
        scheduler.update(elevator, new Event(EventType.MAINTENANCE_REQUIRED, null));
        assertEquals(0, dispatchCount.get());
        Mockito.verify(elevator, Mockito.never()).handleEmergency();
    }

    @Test
    public void testSchedulerStrategySwitchAndSingletons() {
        // 中文注释：验证调度策略动态切换以及两个工厂方法
        List<Elevator> elevators = Collections.singletonList(createElevator(1, 1));
        DispatchStrategy first = (list, request) -> list.get(0);
        Scheduler scheduler = new Scheduler(elevators, 2, first);

        PassengerRequest request = new PassengerRequest(1, 2, Priority.LOW, RequestType.STANDARD);
        scheduler.dispatchElevator(request);

        DispatchStrategy second = (list, req) -> null;
        scheduler.setDispatchStrategy(second);
        scheduler.dispatchElevator(request);

        Scheduler singleton = Scheduler.getInstance(elevators, 2, first);
        Scheduler singletonAgain = Scheduler.getInstance();
        assertSame(singleton, singletonAgain);
    }

    @Test
    public void testSchedulerStrategySwapTakesEffect() {
        // 中文注释：验证切换策略后会立即使用新的选择结果
        Elevator firstElevator = createElevator(4, 1);
        Elevator secondElevator = createElevator(5, 6);
        List<Elevator> elevators = Arrays.asList(firstElevator, secondElevator);
        Scheduler scheduler = new Scheduler(elevators, 6, (list, request) -> list.get(0));

        PassengerRequest firstRequest = new PassengerRequest(2, 5, Priority.LOW, RequestType.STANDARD);
        scheduler.dispatchElevator(firstRequest);
        assertTrue(firstElevator.getDestinationSet().contains(firstRequest.getStartFloor()));

        scheduler.setDispatchStrategy((list, request) -> list.get(1));
        PassengerRequest secondRequest = new PassengerRequest(3, 8, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.dispatchElevator(secondRequest);
        assertTrue(secondElevator.getDestinationSet().contains(secondRequest.getStartFloor()));
        assertFalse(secondElevator.getDestinationSet().contains(firstRequest.getStartFloor()));
    }

    @Test
    public void testSchedulerExecuteEmergencyProtocolInvokesAll() {
        // 中文注释：验证应急协议会逐一调用所有电梯的应急流程
        Elevator e1 = Mockito.spy(createElevator(1, 3));
        Elevator e2 = Mockito.spy(createElevator(2, 8));
        List<Elevator> elevators = new ArrayList<>();
        elevators.add(e1);
        elevators.add(e2);
        Scheduler scheduler = new Scheduler(elevators, 1, new NearestElevatorStrategy());
        scheduler.executeEmergencyProtocol();
        Mockito.verify(e1).handleEmergency();
        Mockito.verify(e2).handleEmergency();
    }

    @Test
    public void testSchedulerDefaultSingletonInitialization() throws Exception {
        // 中文注释：验证无参单例模式下的默认结构
        Scheduler scheduler = Scheduler.getInstance();
        @SuppressWarnings("unchecked")
        List<Elevator> elevatorList = getPrivateField(scheduler, "elevatorList");
        Map<Integer, Floor> floors = getPrivateField(scheduler, "floors");
        assertNotNull(elevatorList);
        assertTrue(floors.isEmpty());
    }

    @Test
    public void testElevatorInitialStateAndObservers() {
        // 中文注释：验证电梯初始状态及观察者通知
        Elevator elevator = createElevator(5, 1);
        assertEquals(1, elevator.getCurrentFloor());
        assertEquals(Direction.UP, elevator.getDirection());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
        assertEquals(ElevatorMode.NORMAL, elevator.getMode());
        assertTrue(elevator.getPassengerList().isEmpty());

        AtomicReference<Object> observed = new AtomicReference<>();
        Observer observer = (o, arg) -> observed.set(((Event) arg).getType());
        elevator.addObserver(observer);
        elevator.notifyObservers(new Event(EventType.MAINTENANCE_REQUIRED, "test"));
        assertEquals(EventType.MAINTENANCE_REQUIRED, observed.get());
    }

    @Test
    public void testElevatorHandleEmergencySendsStatusNotification() throws Exception {
        // 中文注释：验证紧急处理会清空数据并通知观察者
        Elevator elevator = createElevator(21, 7);
        AtomicReference<Object> observed = new AtomicReference<>();
        elevator.addObserver((o, arg) -> observed.set(arg));
        Field passengerField = Elevator.class.getDeclaredField("passengerList");
        passengerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> passengerList = (List<PassengerRequest>) passengerField.get(elevator);
        passengerList.add(new PassengerRequest(1, 2, Priority.LOW, RequestType.STANDARD));
        elevator.getDestinationSet().add(9);
        elevator.handleEmergency();
        assertEquals(ElevatorStatus.EMERGENCY, observed.get());
        assertTrue(elevator.getPassengerList().isEmpty());
        assertEquals(Collections.singleton(1), new TreeSet<>(elevator.getDestinationSet()));
    }

    @Test
    public void testElevatorRunHandlesEmergencyAndNormalMove() throws Exception {
        // 中文注释：通过真实线程覆盖run方法的两个分支
        Scheduler schedulerMock = Mockito.mock(Scheduler.class);
        Elevator elevator = new Elevator(41, schedulerMock);
        elevator.setCurrentFloor(2);
        elevator.setDirection(Direction.DOWN);

        Thread worker = new Thread(elevator, "elevator-runner");
        worker.start();

        ReentrantLock lock = elevator.getLock();
        lock.lock();
        try {
            elevator.setStatus(ElevatorStatus.EMERGENCY);
            elevator.getDestinationSet().add(1);
            elevator.getCondition().signalAll();
        } finally {
            lock.unlock();
        }

        awaitCondition(() -> elevator.getCurrentFloor() == 1 && elevator.getStatus() == ElevatorStatus.IDLE,
                6000, "紧急返回未完成");

        lock.lock();
        try {
            elevator.setStatus(ElevatorStatus.MOVING);
            elevator.setDirection(Direction.UP);
            elevator.getDestinationSet().add(2);
            elevator.getCondition().signalAll();
        } finally {
            lock.unlock();
        }

        awaitCondition(() -> elevator.getCurrentFloor() == 2,
                6000, "常规移动未完成");

        worker.interrupt();
        worker.join(3000);
        assertFalse("线程应已结束", worker.isAlive());
        assertTrue("能耗应累加", elevator.getEnergyConsumption() > 0);
    }

    @Test
    public void testElevatorStateMutatorsAffectFields() {
        // 中文注释：验证各种setter会正确影响状态
        Elevator elevator = createElevator(9, 4);
        elevator.setMode(ElevatorMode.ENERGY_SAVING);
        elevator.setEnergyConsumption(42.5);
        elevator.setCurrentLoad(150.0);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.DOWN);
        assertEquals(ElevatorMode.ENERGY_SAVING, elevator.getMode());
        assertEquals(42.5, elevator.getEnergyConsumption(), 0.0001);
        assertEquals(150.0, elevator.getCurrentLoad(), 0.0001);
        assertEquals(ElevatorStatus.MOVING, elevator.getStatus());
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test
    public void testElevatorMoveAndDoorOperations() throws Exception {
        // 中文注释：验证move逻辑在上下行时均能触发开门与能耗记录
        Elevator elevator = createElevator(1, 1);
        elevator.getDestinationSet().add(2);
        elevator.move();
        assertEquals(2, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
        assertTrue(elevator.getDestinationSet().isEmpty());
        assertTrue(elevator.getEnergyConsumption() > 0);

        elevator.getDestinationSet().add(1);
        elevator.setCurrentFloor(3);
        elevator.setDirection(Direction.DOWN);
        elevator.move();
        assertEquals(2, elevator.getCurrentFloor());
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test
    public void testElevatorOpenDoorStandalone() throws Exception {
        // 中文注释：直接调用开门流程以覆盖停梯状态
        Scheduler schedulerMock = Mockito.mock(Scheduler.class);
        Elevator elevator = new Elevator(20, schedulerMock);
        elevator.setCurrentFloor(6);
        Field passengerField = Elevator.class.getDeclaredField("passengerList");
        passengerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> passengerList = (List<PassengerRequest>) passengerField.get(elevator);
        passengerList.add(new PassengerRequest(1, 6, Priority.LOW, RequestType.STANDARD));
        Mockito.when(schedulerMock.getRequestsAtFloor(Mockito.anyInt(), Mockito.any(Direction.class)))
                .thenReturn(Collections.emptyList());
        elevator.openDoor();
        assertEquals(ElevatorStatus.STOPPED, elevator.getStatus());
        assertTrue(elevator.getPassengerList().isEmpty());
    }

    @Test
    public void testElevatorLoadUnloadAndDirectionUpdates() throws Exception {
        // 中文注释：验证上下客流程以及方向推断各个分支
        Scheduler schedulerMock = Mockito.mock(Scheduler.class);
        Elevator elevator = new Elevator(2, schedulerMock);
        elevator.setCurrentFloor(5);
        PassengerRequest toStay = new PassengerRequest(1, 5, Priority.LOW, RequestType.STANDARD);
        PassengerRequest toMove = new PassengerRequest(1, 7, Priority.LOW, RequestType.STANDARD);

        Field passengerField = Elevator.class.getDeclaredField("passengerList");
        passengerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> passengerList = (List<PassengerRequest>) passengerField.get(elevator);
        passengerList.add(toStay);
        passengerList.add(toMove);
        elevator.unloadPassengers();
        assertEquals(1, passengerList.size());

        Mockito.when(schedulerMock.getRequestsAtFloor(5, Direction.UP))
                .thenReturn(Arrays.asList(new PassengerRequest(5, 9, Priority.LOW, RequestType.STANDARD),
                        new PassengerRequest(5, 4, Priority.LOW, RequestType.STANDARD)));
        elevator.setCurrentLoad(elevator.getMaxLoad() - 10);
        elevator.loadPassengers();
        assertTrue(elevator.getCurrentLoad() > elevator.getMaxLoad() - 10);

        elevator.getDestinationSet().clear();
        elevator.updateDirection();
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());

        elevator.getDestinationSet().add(8);
        elevator.setCurrentFloor(6);
        elevator.updateDirection();
        assertEquals(Direction.UP, elevator.getDirection());

        elevator.getDestinationSet().add(2);
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test
    public void testElevatorUpdateDirectionWhenMinEqualsCurrent() {
        // 中文注释：验证最小目标楼层等于当前楼层时方向会被置为DOWN
        Elevator elevator = createElevator(60, 5);
        elevator.getDestinationSet().add(5);
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test
    public void testElevatorLoadPassengersRespectsMaxLoad() throws Exception {
        // 中文注释：验证载客量达到上限时不会继续装载
        Scheduler schedulerMock = Mockito.mock(Scheduler.class);
        Elevator elevator = new Elevator(30, schedulerMock);
        elevator.setCurrentFloor(9);
        Mockito.when(schedulerMock.getRequestsAtFloor(9, Direction.UP))
                .thenReturn(Arrays.asList(
                        new PassengerRequest(9, 10, Priority.LOW, RequestType.STANDARD),
                        new PassengerRequest(9, 12, Priority.MEDIUM, RequestType.STANDARD)));
        elevator.setCurrentLoad(elevator.getMaxLoad());
        Field passengerField = Elevator.class.getDeclaredField("passengerList");
        passengerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> passengerList = (List<PassengerRequest>) passengerField.get(elevator);
        passengerList.clear();
        elevator.loadPassengers();
        assertTrue(passengerList.isEmpty());
        assertTrue(elevator.getDestinationSet().isEmpty());
    }

    @Test
    public void testElevatorLoadPassengersUsesCurrentDirection() {
        // 中文注释：验证载客时会根据当前方向向调度器取数
        Scheduler schedulerMock = Mockito.mock(Scheduler.class);
        Elevator elevator = new Elevator(56, schedulerMock);
        elevator.setCurrentFloor(6);
        elevator.setDirection(Direction.DOWN);
        PassengerRequest down = new PassengerRequest(6, 2, Priority.LOW, RequestType.STANDARD);
        Mockito.when(schedulerMock.getRequestsAtFloor(6, Direction.DOWN))
                .thenReturn(Collections.singletonList(down));

        elevator.loadPassengers();
        Mockito.verify(schedulerMock).getRequestsAtFloor(6, Direction.DOWN);
        assertTrue(elevator.getPassengerList().contains(down));
        assertTrue(elevator.getDestinationSet().contains(down.getDestinationFloor()));
    }

    @Test
    public void testElevatorDestinationsAndClearRequests() throws Exception {
        // 中文注释：验证目的地集合为TreeSet并且清空请求时返回副本
        Elevator elevator = createElevator(3, 1);
        elevator.addDestination(5);
        elevator.addDestination(3);
        elevator.addDestination(4);
        Set<Integer> destinations = elevator.getDestinationSet();
        assertEquals(Arrays.asList(3, 4, 5), new ArrayList<>(destinations));

        Field passengerField = Elevator.class.getDeclaredField("passengerList");
        passengerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> passengerList = (List<PassengerRequest>) passengerField.get(elevator);
        passengerList.add(new PassengerRequest(1, 3, Priority.LOW, RequestType.STANDARD));
        List<PassengerRequest> cleared = elevator.clearAllRequests();
        assertEquals(1, cleared.size());
        assertTrue(elevator.getDestinationSet().isEmpty());
    }

    @Test
    public void testElevatorClearAllRequestsReturnsIndependentCopy() throws Exception {
        // 中文注释：验证clearAllRequests返回的集合与内部状态完全隔离
        Elevator elevator = createElevator(31, 2);
        Field passengerField = Elevator.class.getDeclaredField("passengerList");
        passengerField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PassengerRequest> passengerList = (List<PassengerRequest>) passengerField.get(elevator);
        passengerList.add(new PassengerRequest(2, 5, Priority.LOW, RequestType.STANDARD));
        passengerList.add(new PassengerRequest(3, 6, Priority.MEDIUM, RequestType.STANDARD));
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(9);
        List<PassengerRequest> snapshot = elevator.clearAllRequests();
        assertEquals(2, snapshot.size());
        snapshot.clear();
        assertTrue(elevator.getPassengerList().isEmpty());
        assertTrue(elevator.getDestinationSet().isEmpty());
    }

    @Test
    public void testElevatorAddDestinationSignalsWaitingThread() throws Exception {
        // 中文注释：验证addDestination会唤醒等待线程
        Elevator elevator = createElevator(70, 1);
        CountDownLatch waiting = new CountDownLatch(1);
        CountDownLatch released = new CountDownLatch(1);

        Thread waiter = new Thread(() -> {
            ReentrantLock lock = elevator.getLock();
            lock.lock();
            try {
                waiting.countDown();
                elevator.getCondition().await();
                released.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });
        waiter.start();

        assertTrue(waiting.await(1, TimeUnit.SECONDS));
        elevator.addDestination(8);
        assertTrue("等待线程应被唤醒", released.await(2, TimeUnit.SECONDS));

        waiter.interrupt();
        waiter.join(1000);
        assertTrue(elevator.getDestinationSet().contains(8));
    }

    @Test
    public void testElevatorMoveToFirstFloorAndEmergencyHandling() throws Exception {
        // 中文注释：验证紧急回到一层与状态恢复逻辑
        Elevator elevator = createElevator(4, 3);
        elevator.setDirection(Direction.DOWN);
        elevator.moveToFirstFloor();
        assertEquals(1, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());

        elevator.setCurrentFloor(0);
        elevator.setDirection(Direction.UP);
        elevator.moveToFirstFloor();
        assertEquals(1, elevator.getCurrentFloor());

        elevator.setCurrentFloor(1);
        elevator.handleEmergency();
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
        assertEquals(Collections.singleton(1), new TreeSet<>(elevator.getDestinationSet()));
    }

    @Test
    public void testElevatorStatusReportToString() {
        // 中文注释：验证状态报表封装的各个字段
        ElevatorStatusReport report = new ElevatorStatusReport(10, 12, Direction.DOWN, ElevatorStatus.MOVING, 1.2, 120.5, 6);
        assertTrue(report.toString().contains("elevatorId=10"));
        assertEquals(10, report.getElevatorId());
        assertEquals(12, report.getCurrentFloor());
        assertEquals(Direction.DOWN, report.getDirection());
        assertEquals(ElevatorStatus.MOVING, report.getStatus());
        assertEquals(1.2, report.getSpeed(), 0.0001);
        assertEquals(120.5, report.getCurrentLoad(), 0.0001);
        assertEquals(6, report.getPassengerCount());
    }

    private static class TestMaintenanceManager extends MaintenanceManager {
        private final List<MaintenanceTask> notified = new ArrayList<>();
        private final List<MaintenanceRecord> records = new ArrayList<>();
        private final List<String> notifications = new ArrayList<>();
        private boolean performed;

        @Override
        public void processTasks() {
            try {
                Field field = MaintenanceManager.class.getDeclaredField("taskQueue");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Queue<MaintenanceTask> queue = (Queue<MaintenanceTask>) field.get(this);
                MaintenanceTask task;
                while ((task = queue.poll()) != null) {
                    performMaintenance(task);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void notifyMaintenancePersonnel(MaintenanceTask task) {
            notified.add(task);
            notifications.add("notify-" + task.getElevatorId());
        }

        @Override
        public void performMaintenance(MaintenanceTask task) {
            performed = true;
            records.add(new MaintenanceRecord(task.getElevatorId(), System.currentTimeMillis(), task.getDescription()));
        }

        public List<MaintenanceTask> getNotified() {
            return notified;
        }

        public List<MaintenanceRecord> getRecords() {
            return records;
        }

        public List<String> getNotificationLog() {
            return notifications;
        }

        public boolean isPerformed() {
            return performed;
        }
    }

    @Test
    public void testMaintenanceManagerSchedulingAndRecords() {
        // 中文注释：验证维护管理器接收事件、排队任务并生成记录
        TestMaintenanceManager manager = new TestMaintenanceManager();
        Elevator elevator = createElevator(11, 7);
        manager.scheduleMaintenance(elevator);
        manager.processTasks();

        assertEquals(1, manager.getNotified().size());
        assertEquals(1, manager.getRecords().size());
        assertTrue(manager.isPerformed());

        EventBus.Event event = new EventBus.Event(EventType.ELEVATOR_FAULT, elevator);
        manager.onEvent(event);
        manager.processTasks();
        assertEquals(2, manager.getNotified().size());

        MaintenanceManager.MaintenanceTask task = new MaintenanceManager.MaintenanceTask(1, 2L, "desc");
        assertEquals(1, task.getElevatorId());
        assertEquals(2L, task.getScheduledTime());
        assertEquals("desc", task.getDescription());

        MaintenanceManager.MaintenanceRecord record = new MaintenanceManager.MaintenanceRecord(3, 4L, "done");
        assertEquals(3, record.getElevatorId());
        assertEquals(4L, record.getMaintenanceTime());
        assertEquals("done", record.getResult());

        manager.recordMaintenanceResult(9, "manual");
        try {
            List<MaintenanceManager.MaintenanceRecord> stored = getPrivateField(manager, "maintenanceRecords");
            assertFalse(stored.isEmpty());
        } catch (Exception e) {
            fail("无法读取维护记录: " + e.getMessage());
        }

        assertTrue(manager.getNotificationLog().stream().anyMatch(s -> s.contains(String.valueOf(elevator.getId()))));

        // 关闭内部执行器，避免后台线程影响后续测试
        try {
            ExecutorService executor = getPrivateField(manager, "executorService");
            executor.shutdownNow();
        } catch (Exception e) {
            fail("无法关闭维护管理器线程: " + e.getMessage());
        }
    }

    @Test
    public void testMaintenanceManagerBackgroundThreadProcessesTasks() throws Exception {
        // 中文注释：验证真实后台线程能够消费队列并写入记录
        MaintenanceManager manager = new MaintenanceManager();
        Elevator elevator = createElevator(50, 9);
        manager.scheduleMaintenance(elevator);
        awaitCondition(() -> {
            try {
                List<MaintenanceManager.MaintenanceRecord> records = getPrivateField(manager, "maintenanceRecords");
                return !records.isEmpty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 2000, "后台维护线程未及时写入记录");
        List<MaintenanceManager.MaintenanceRecord> records = getPrivateField(manager, "maintenanceRecords");
        assertFalse(records.isEmpty());
        ExecutorService executor = getPrivateField(manager, "executorService");
        executor.shutdownNow();
    }

    @Test
    public void testMaintenanceManagerOnEventIgnoresNonFault() {
        // 中文注释：验证非故障事件不会触发维护任务
        TestMaintenanceManager manager = new TestMaintenanceManager();
        manager.onEvent(new EventBus.Event(EventType.MAINTENANCE_REQUIRED, createElevator(60, 3)));
        manager.processTasks();
        assertTrue(manager.getNotified().isEmpty());
        try {
            ExecutorService executor = getPrivateField(manager, "executorService");
            executor.shutdownNow();
        } catch (Exception e) {
            fail("无法关闭维护管理器线程: " + e.getMessage());
        }
    }

    @Test
    public void testMaintenanceManagerTaskQueueHasFIFOOrdering() throws Exception {
        // 中文注释：验证任务队列按照 FIFO 顺序处理
        TestMaintenanceManager manager = new TestMaintenanceManager();
        Elevator e1 = createElevator(70, 1);
        Elevator e2 = createElevator(71, 2);
        manager.scheduleMaintenance(e1);
        manager.scheduleMaintenance(e2);
        manager.processTasks();
        List<MaintenanceManager.MaintenanceTask> notified = manager.getNotified();
        assertEquals(2, notified.size());
        assertEquals(e1.getId(), notified.get(0).getElevatorId());
        assertEquals(e2.getId(), notified.get(1).getElevatorId());
        ExecutorService executor = getPrivateField(manager, "executorService");
        executor.shutdownNow();
    }

    @Test
    public void testMaintenanceManagerScheduleAddsToQueueBeforeProcessing() throws Exception {
        // 中文注释：验证调度任务会按顺序进入队列
        class PassiveMaintenanceManager extends MaintenanceManager {
            private final CountDownLatch started = new CountDownLatch(1);
            private final CountDownLatch release = new CountDownLatch(1);

            @Override
            public void processTasks() {
                started.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            void awaitStart() throws InterruptedException {
                assertTrue("后台线程未启动", started.await(2, TimeUnit.SECONDS));
            }

            void allowExit() {
                release.countDown();
            }
        }
        PassiveMaintenanceManager manager = new PassiveMaintenanceManager();
        manager.awaitStart();
        Elevator e1 = createElevator(75, 5);
        Elevator e2 = createElevator(76, 6);
        manager.scheduleMaintenance(e1);
        manager.scheduleMaintenance(e2);
        @SuppressWarnings("unchecked")
        Queue<MaintenanceManager.MaintenanceTask> queue = getPrivateField(manager, "taskQueue");
        assertEquals(2, queue.size());
        assertEquals(e1.getId(), queue.peek().getElevatorId());
        manager.allowExit();
        ExecutorService executor = getPrivateField(manager, "executorService");
        executor.shutdownNow();
    }

    private static class StubScheduler extends Scheduler {
        private boolean triggered;

        StubScheduler() {
            super(new ArrayList<>(), 0, new NearestElevatorStrategy());
        }

        @Override
        public void executeEmergencyProtocol() {
            triggered = true;
        }

        boolean isTriggered() {
            return triggered;
        }
    }

    @Test
    public void testSecurityMonitorHandlesEmergencyEvent() throws Exception {
        // 中文注释：验证安全监控可响应事件并触发后续流程
        StubScheduler fakeScheduler = new StubScheduler();
        Field schedulerInstance = Scheduler.class.getDeclaredField("instance");
        schedulerInstance.setAccessible(true);
        schedulerInstance.set(null, fakeScheduler);

        SecurityMonitor monitor = new SecurityMonitor();
        long start = System.currentTimeMillis();
        monitor.handleEmergency("fire");
        long end = System.currentTimeMillis();

        List<SecurityMonitor.SecurityEvent> events = getPrivateField(monitor, "securityEvents");
        assertEquals(1, events.size());
        assertEquals("Emergency situation", events.get(0).getDescription());
        assertEquals("fire", events.get(0).getData());

        List<LogManager.SystemLog> logs = LogManager.getInstance().queryLogs("SecurityMonitor", start, end);
        assertFalse(logs.isEmpty());

        EventBus.getInstance().publish(new EventBus.Event(EventType.EMERGENCY, "bus"));
        assertTrue(fakeScheduler.isTriggered());

        // 清理监控器线程与单例引用
        ExecutorService executor = getPrivateField(monitor, "executorService");
        executor.shutdownNow();
        schedulerInstance.set(null, null);
    }

    @Test
    public void testSecurityMonitorLocksAndNotifications() throws Exception {
        // 中文注释：验证安全监控对锁与通知的使用
        StubScheduler stubScheduler = new StubScheduler();
        Field schedulerInstance = Scheduler.class.getDeclaredField("instance");
        schedulerInstance.setAccessible(true);
        schedulerInstance.set(null, stubScheduler);

        SecurityMonitor monitor = new SecurityMonitor();
        NotificationService service = NotificationService.getInstance();
        @SuppressWarnings("unchecked")
        List<NotificationService.NotificationChannel> channels = getPrivateField(service, "channels");
        List<NotificationService.NotificationChannel> backup = new ArrayList<>(channels);
        AtomicReference<NotificationService.Notification> captured = new AtomicReference<>();
        NotificationService.NotificationChannel spyChannel = new NotificationService.NotificationChannel() {
            @Override
            public boolean supports(NotificationService.NotificationType type) {
                return true;
            }

            @Override
            public void send(NotificationService.Notification notification) {
                captured.set(notification);
            }
        };
        channels.clear();
        channels.add(spyChannel);

        EventBus.getInstance().publish(new EventBus.Event(EventType.EMERGENCY, "lockTest"));
        awaitCondition(() -> {
            try {
                List<SecurityMonitor.SecurityEvent> events = getPrivateField(monitor, "securityEvents");
                return !events.isEmpty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 2000, "安全事件未记录");
        List<SecurityMonitor.SecurityEvent> events = getPrivateField(monitor, "securityEvents");
        assertEquals("lockTest", events.get(0).getData());
        assertNotNull(captured.get());
        assertTrue(stubScheduler.isTriggered());

        ExecutorService executor = getPrivateField(monitor, "executorService");
        executor.shutdownNow();
        schedulerInstance.set(null, null);
        channels.clear();
        channels.addAll(backup);
    }

    @Test
    public void testSecurityMonitorIgnoresNonEmergencyEvents() throws Exception {
        // 中文注释：验证非EMERGENCY事件不触发任何动作
        SecurityMonitor monitor = new SecurityMonitor();
        NotificationService service = NotificationService.getInstance();
        @SuppressWarnings("unchecked")
        List<NotificationService.NotificationChannel> channels = getPrivateField(service, "channels");
        List<NotificationService.NotificationChannel> backup = new ArrayList<>(channels);
        AtomicInteger counter = new AtomicInteger();
        NotificationService.NotificationChannel countingChannel = new NotificationService.NotificationChannel() {
            @Override
            public boolean supports(NotificationService.NotificationType type) {
                return true;
            }

            @Override
            public void send(NotificationService.Notification notification) {
                counter.incrementAndGet();
            }
        };
        channels.clear();
        channels.add(countingChannel);
        try {
            monitor.onEvent(new EventBus.Event(EventType.MAINTENANCE_REQUIRED, "noop"));
            List<SecurityMonitor.SecurityEvent> events = getPrivateField(monitor, "securityEvents");
            assertTrue(events.isEmpty());
            assertEquals(0, counter.get());
        } finally {
            channels.clear();
            channels.addAll(backup);
            ExecutorService executor = getPrivateField(monitor, "executorService");
            executor.shutdownNow();
        }
    }

    @Test
    public void testSecurityMonitorSecurityEventValueObject() {
        // 中文注释：验证安全事件实体封装的字段
        SecurityMonitor.SecurityEvent event = new SecurityMonitor.SecurityEvent("desc", 123L, "payload");
        assertEquals("desc", event.getDescription());
        assertEquals(123L, event.getTimestamp());
        assertEquals("payload", event.getData());
    }

    @Test
    public void testSecurityMonitorSingletonAutoSubscription() throws Exception {
        // 中文注释：验证单例获取与自动订阅事件总线
        StubScheduler stubScheduler = new StubScheduler();
        Field schedulerInstance = Scheduler.class.getDeclaredField("instance");
        schedulerInstance.setAccessible(true);
        schedulerInstance.set(null, stubScheduler);

        SecurityMonitor monitor1 = SecurityMonitor.getInstance();
        SecurityMonitor monitor2 = SecurityMonitor.getInstance();
        assertSame(monitor1, monitor2);

        EventBus.getInstance().publish(new EventBus.Event(EventType.EMERGENCY, "singleton"));
        awaitCondition(() -> {
            try {
                List<SecurityMonitor.SecurityEvent> events = getPrivateField(monitor1, "securityEvents");
                return !events.isEmpty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 2000, "单例订阅未响应");

        List<SecurityMonitor.SecurityEvent> events = getPrivateField(monitor1, "securityEvents");
        assertEquals("singleton", events.get(events.size() - 1).getData());
        assertTrue(stubScheduler.isTriggered());

        ExecutorService executor = getPrivateField(monitor1, "executorService");
        executor.shutdownNow();
        schedulerInstance.set(null, null);
        Field monitorInstance = SecurityMonitor.class.getDeclaredField("instance");
        monitorInstance.setAccessible(true);
        monitorInstance.set(null, null);
    }

    @Test
    public void testThreadPoolManagerTaskExecution() throws InterruptedException {

        ThreadPoolManager manager = new ThreadPoolManager();
        CountDownLatch latch = new CountDownLatch(1);
        manager.submitTask(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        manager.shutdown();
    }

    @Test
    public void testThreadPoolManagerShutdownBranches() throws Exception {
        // 中文注释：通过注入Mock执行器覆盖等待失败和中断分支
        ThreadPoolManager manager = new ThreadPoolManager();
        ExecutorService executor = Mockito.mock(ExecutorService.class);
        Mockito.when(executor.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(false);
        ExecutorService original = getPrivateField(manager, "executorService");
        original.shutdownNow();
        setPrivateField(manager, "executorService", executor);
        manager.shutdown();
        Mockito.verify(executor).shutdown();
        Mockito.verify(executor).shutdownNow();

        ThreadPoolManager interruptedManager = new ThreadPoolManager();
        ExecutorService interruptedExecutor = Mockito.mock(ExecutorService.class);
        Mockito.when(interruptedExecutor.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenThrow(new InterruptedException("test"));
        ExecutorService interruptedOriginal = getPrivateField(interruptedManager, "executorService");
        interruptedOriginal.shutdownNow();
        setPrivateField(interruptedManager, "executorService", interruptedExecutor);
        interruptedManager.shutdown();
        Mockito.verify(interruptedExecutor).shutdownNow();
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // 清理中断标记，避免影响后续测试
    }

    @Test
    public void testThreadPoolManagerShutdownSuccessPath() throws Exception {
        // 中文注释：验证正常关闭时不会调用shutdownNow
        ThreadPoolManager manager = new ThreadPoolManager();
        ExecutorService executor = Mockito.mock(ExecutorService.class);
        Mockito.when(executor.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(true);
        ExecutorService original = getPrivateField(manager, "executorService");
        original.shutdownNow();
        setPrivateField(manager, "executorService", executor);
        manager.shutdown();
        Mockito.verify(executor).shutdown();
        Mockito.verify(executor, Mockito.never()).shutdownNow();
    }

    @Test
    public void testThreadPoolManagerSingletonAndSubmit() throws Exception {
        // 中文注释：验证单例模式与任务提交流程
        Field instanceField = ThreadPoolManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        ThreadPoolManager singleton = ThreadPoolManager.getInstance();
        assertSame(singleton, ThreadPoolManager.getInstance());

        CountDownLatch latch = new CountDownLatch(2);
        singleton.submitTask(latch::countDown);
        singleton.submitTask(latch::countDown);
        assertTrue("单例线程池应执行提交的任务", latch.await(2, TimeUnit.SECONDS));

        ExecutorService executor = getPrivateField(singleton, "executorService");
        executor.shutdownNow();
        instanceField.set(null, null);
    }
}
