package net.mooctest;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ElevatorManagerTest {

    @Before
    public void setUp() throws Exception {
        cleanupSingletons();
    }

    @After
    public void tearDown() throws Exception {
        cleanupSingletons();
    }

    private void cleanupSingletons() throws Exception {
        resetSingletonWithExecutor(MaintenanceManager.class, "instance", "executorService");
        resetSingletonWithExecutor(SecurityMonitor.class, "instance", "executorService");
        resetSingletonWithExecutor(ThreadPoolManager.class, "instance", "executorService");
        resetSingleton(SystemConfig.class, "instance");
        resetSingleton(NotificationService.class, "instance");
        resetSingleton(AnalyticsEngine.class, "instance");
        resetSingleton(LogManager.class, "instance");
        resetSingleton(ElevatorManager.class, "instance");
        resetSingleton(EventBus.class, "instance");
        resetSingleton(Scheduler.class, "instance");
    }

    private Field findField(Class<?> type, String fieldName) throws Exception {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field field = findField(clazz, fieldName);
        field.set(null, null);
    }

    private void resetSingletonWithExecutor(Class<?> clazz, String instanceField, String executorField) throws Exception {
        Field field = clazz.getDeclaredField(instanceField);
        field.setAccessible(true);
        Object instance = field.get(null);
        if (instance != null) {
            Field execField = clazz.getDeclaredField(executorField);
            execField.setAccessible(true);
            ExecutorService executor = (ExecutorService) execField.get(instance);
            executor.shutdownNow();
        }
        field.set(null, null);
    }

    private void shutdownExecutorField(Object target, String fieldName) throws Exception {
        Field execField = findField(target.getClass(), fieldName);
        ExecutorService executor = (ExecutorService) execField.get(target);
        executor.shutdownNow();
    }

    private void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.set(target, value);
    }

    private void setStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = findField(clazz, fieldName);
        field.set(null, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        return (T) field.get(target);
    }

    @SuppressWarnings("unchecked")
    private void addPassenger(Elevator elevator, PassengerRequest request) throws Exception {
        Field field = Elevator.class.getDeclaredField("passengerList");
        field.setAccessible(true);
        List<PassengerRequest> passengers = (List<PassengerRequest>) field.get(elevator);
        passengers.add(request);
        Field loadField = Elevator.class.getDeclaredField("currentLoad");
        loadField.setAccessible(true);
        loadField.setDouble(elevator, passengers.size() * 70);
    }

    @SuppressWarnings("unchecked")
    private void addDestination(Elevator elevator, int floor) throws Exception {
        Field field = Elevator.class.getDeclaredField("destinationSet");
        field.setAccessible(true);
        Set<Integer> destinations = (Set<Integer>) field.get(elevator);
        destinations.add(floor);
    }

    private FastElevator buildElevator(int id, Scheduler scheduler, int floor, Direction direction, ElevatorStatus status) {
        FastElevator elevator = new FastElevator(id, scheduler);
        elevator.setCurrentFloor(floor);
        elevator.setDirection(direction);
        elevator.setStatus(status);
        return elevator;
    }

    private String captureOutput(Runnable runnable) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            runnable.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    // --------------------------------- Tests ---------------------------------

    @Test
    public void testPassengerRequestDirectionAndToString() {
        // 该测试验证乘客请求在不同楼层组合下的方向推断和字符串输出是否正确
        PassengerRequest up = new PassengerRequest(1, 6, Priority.HIGH, RequestType.STANDARD);
        assertEquals(Direction.UP, up.getDirection());
        String upString = up.toString();
        assertTrue(upString.contains("From 1"));
        assertTrue(upString.contains("Priority: HIGH"));

        PassengerRequest down = new PassengerRequest(10, 2, Priority.LOW, RequestType.DESTINATION_CONTROL);
        assertEquals(Direction.DOWN, down.getDirection());
        assertTrue(down.toString().contains("to 2"));
    }

    @Test
    public void testSystemConfigValidation() {
        // 该测试验证系统配置的正值更新和对非法值的忽略行为
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(30);
        config.setElevatorCount(8);
        config.setMaxLoad(600);
        config.setFloorCount(-1);
        config.setElevatorCount(0);
        config.setMaxLoad(-10);
        assertEquals(30, config.getFloorCount());
        assertEquals(8, config.getElevatorCount());
        assertEquals(600, config.getMaxLoad(), 0.0);
    }

    @Test
    public void testFloorRequestsForBothDirections() {
        // 该测试验证楼层队列可以分别缓存上下行请求并在读取后清空
        Floor floor = new Floor(5);
        PassengerRequest up = new PassengerRequest(5, 8, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest down = new PassengerRequest(5, 2, Priority.LOW, RequestType.STANDARD);
        floor.addRequest(up);
        floor.addRequest(down);
        assertEquals(5, floor.getFloorNumber());
        List<PassengerRequest> upRequests = floor.getRequests(Direction.UP);
        assertEquals(1, upRequests.size());
        assertEquals(0, floor.getRequests(Direction.UP).size());
        List<PassengerRequest> downRequests = floor.getRequests(Direction.DOWN);
        assertEquals(1, downRequests.size());
    }

    @Test
    public void testElevatorMoveUpFlow() throws Exception {
        // 该测试验证电梯向上行驶、能耗累计和到站后状态切换
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(1, scheduler);
        elevators.add(elevator);
        elevator.setCurrentFloor(1);
        elevator.addDestination(3);
        elevator.addDestination(5);
        elevator.move();
        assertEquals(2, elevator.getCurrentFloor());
        assertEquals(Direction.UP, elevator.getDirection());
        elevator.move();
        elevator.move();
        assertEquals(4, elevator.getCurrentFloor());
        elevator.move();
        assertEquals(5, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
        assertEquals(4.0, elevator.getEnergyConsumption(), 0.001);
        assertTrue(elevator.getDestinationSet().isEmpty());
    }

    @Test
    public void testElevatorMoveDownFlow() throws Exception {
        // 该测试验证电梯向下行驶时的方向更新和目的地判定逻辑
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(2, scheduler);
        elevators.add(elevator);
        elevator.setCurrentFloor(5);
        elevator.addDestination(2);
        elevator.move();
        assertEquals(Direction.DOWN, elevator.getDirection());
        assertTrue(elevator.getDestinationSet().contains(2));
        assertEquals(4, elevator.getCurrentFloor());
        elevator.move();
        elevator.move();
        assertEquals(2, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }

    @Test
    public void testElevatorLoadAndUnloadPassengers() throws Exception {
        // 该测试验证电梯载客时的最大载重限制以及卸客后的载重更新
        SystemConfig.getInstance().setMaxLoad(140);
        List<Elevator> elevators = new ArrayList<>();
        StubScheduler stubScheduler = new StubScheduler(elevators, 10);
        FastElevator elevator = new FastElevator(3, stubScheduler);
        elevators.add(elevator);
        elevator.setCurrentFloor(4);
        elevator.setDirection(Direction.UP);
        PassengerRequest first = new PassengerRequest(4, 6, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest second = new PassengerRequest(4, 7, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest third = new PassengerRequest(4, 8, Priority.MEDIUM, RequestType.STANDARD);
        stubScheduler.presetRequests(4, Direction.UP, Arrays.asList(first, second, third));
        elevator.loadPassengers();
        assertEquals(2, elevator.getPassengerList().size());
        assertEquals(140.0, elevator.getCurrentLoad(), 0.001);
        assertTrue(elevator.getDestinationSet().containsAll(Arrays.asList(6, 7)));
        elevator.setCurrentFloor(6);
        elevator.unloadPassengers();
        assertEquals(1, elevator.getPassengerList().size());
        assertEquals(70.0, elevator.getCurrentLoad(), 0.001);
    }

    @Test
    public void testElevatorHandleEmergencyNotifiesObservers() throws Exception {
        // 该测试验证紧急处理时会清空请求、添加一楼目的地并通知观察者
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 5, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(4, scheduler);
        elevators.add(elevator);
        PassengerRequest request = new PassengerRequest(2, 5, Priority.HIGH, RequestType.STANDARD);
        addPassenger(elevator, request);
        addDestination(elevator, 5);
        AtomicReference<Object> notified = new AtomicReference<>();
        Observer observer = (o, arg) -> notified.set(arg);
        elevator.addObserver(observer);
        elevator.handleEmergency();
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
        assertEquals(1, elevator.getDestinationSet().size());
        assertTrue(elevator.getDestinationSet().contains(1));
        assertEquals(0, elevator.getPassengerList().size());
        assertEquals(ElevatorStatus.EMERGENCY, notified.get());
    }

    @Test
    public void testElevatorClearAllRequests() throws Exception {
        // 该测试验证清空请求时返回的列表内容以及内部状态的复位情况
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 5, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(5, scheduler);
        elevators.add(elevator);
        PassengerRequest request = new PassengerRequest(3, 9, Priority.LOW, RequestType.STANDARD);
        addPassenger(elevator, request);
        addDestination(elevator, 9);
        List<PassengerRequest> cleared = elevator.clearAllRequests();
        assertEquals(1, cleared.size());
        assertTrue(elevator.getDestinationSet().isEmpty());
        assertEquals(0, elevator.getPassengerList().size());
    }

    @Test
    public void testElevatorMoveToFirstFloorBothDirections() throws Exception {
        // 该测试验证紧急回一楼时上下行逻辑以及能耗累计
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 5, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(6, scheduler);
        elevator.setCurrentFloor(2);
        elevator.setDirection(Direction.DOWN);
        elevator.moveToFirstFloor();
        assertEquals(1, elevator.getCurrentFloor());
        double energyAfterDown = elevator.getEnergyConsumption();
        elevator.setCurrentFloor(0);
        elevator.setDirection(Direction.UP);
        elevator.moveToFirstFloor();
        assertEquals(1, elevator.getCurrentFloor());
        assertTrue(elevator.getEnergyConsumption() > energyAfterDown);
    }

    @Test
    public void testElevatorOpenDoorOriginalBehavior() throws Exception {
        // 该测试验证开门流程包含停靠、卸客、载客以及睡眠等待
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 5, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(7, scheduler);
        elevator.setCurrentFloor(3);
        PassengerRequest request = new PassengerRequest(1, 3, Priority.MEDIUM, RequestType.STANDARD);
        addPassenger(elevator, request);
        elevator.openDoor();
        assertEquals(ElevatorStatus.STOPPED, elevator.getStatus());
        assertEquals(0, elevator.getPassengerList().size());
        assertEquals(0.0, elevator.getCurrentLoad(), 0.001);
    }

    @Test
    public void testElevatorCustomNotifyObserversEventPayload() {
        // 该测试验证自定义事件对象可以通过notifyObservers方法传递给观察者
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 5, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(8, scheduler);
        elevators.add(elevator);
        AtomicReference<Object> payload = new AtomicReference<>();
        elevator.addObserver((o, arg) -> payload.set(arg));
        Event event = new Event(EventType.MAINTENANCE_REQUIRED, "test");
        elevator.notifyObservers(event);
        assertSame(event, payload.get());
    }

    @Test
    public void testElevatorUpdateDirectionEqualityBranch() throws Exception {
        // 该测试验证updateDirection在目标楼层等于当前楼层时会选择向下分支
        SystemConfig.getInstance().setMaxLoad(500);
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 5, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(31, scheduler);
        elevator.setCurrentFloor(5);
        elevator.setDirection(Direction.UP);
        addDestination(elevator, 5);
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test(timeout = 3000)
    public void testElevatorRunLoopProcessesMove() throws Exception {
        // 该测试验证run循环在收到目的地后能够执行一次移动并响应中断退出
        SystemConfig.getInstance().setMaxLoad(500);
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 5, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(32, scheduler);
        elevator.setCurrentFloor(1);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.addDestination(2);
        Thread worker = new Thread(elevator);
        worker.start();
        Thread.sleep(200);
        worker.interrupt();
        worker.join(1000);
        assertTrue(elevator.getCurrentFloor() >= 2);
    }

    @Test
    public void testNearestElevatorStrategySelectionAndEligibility() {
        // 该测试验证最近电梯策略的距离比较和可派遣判定
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 1, new NearestElevatorStrategy());
        FastElevator idleElevator = buildElevator(9, scheduler, 3, Direction.UP, ElevatorStatus.IDLE);
        FastElevator movingSame = buildElevator(10, scheduler, 6, Direction.UP, ElevatorStatus.MOVING);
        FastElevator movingOpposite = buildElevator(11, scheduler, 2, Direction.DOWN, ElevatorStatus.MOVING);
        elevators.add(idleElevator);
        elevators.add(movingSame);
        elevators.add(movingOpposite);
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        PassengerRequest request = new PassengerRequest(5, 9, Priority.MEDIUM, RequestType.STANDARD);
        assertTrue(strategy.isEligible(idleElevator, request));
        assertTrue(strategy.isEligible(movingSame, request));
        assertFalse(strategy.isEligible(movingOpposite, request));
        Elevator selected = strategy.selectElevator(elevators, request);
        assertEquals(idleElevator, selected);
    }

    @Test
    public void testNearestElevatorStrategyNoEligibleElevator() {
        // 该测试验证当所有电梯都不符合条件时返回null
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 1, new NearestElevatorStrategy());
        FastElevator downOne = buildElevator(35, scheduler, 8, Direction.DOWN, ElevatorStatus.MOVING);
        FastElevator downTwo = buildElevator(36, scheduler, 6, Direction.DOWN, ElevatorStatus.MOVING);
        elevators.add(downOne);
        elevators.add(downTwo);
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        PassengerRequest request = new PassengerRequest(3, 7, Priority.LOW, RequestType.STANDARD);
        assertNull(strategy.selectElevator(elevators, request));
    }

    @Test
    public void testHighEfficiencyStrategyIsCloser() {
        // 该测试验证高效策略的距离比较函数以及筛选逻辑
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 1, new NearestElevatorStrategy());
        FastElevator first = buildElevator(12, scheduler, 2, Direction.UP, ElevatorStatus.IDLE);
        FastElevator second = buildElevator(13, scheduler, 10, Direction.UP, ElevatorStatus.MOVING);
        elevators.add(first);
        elevators.add(second);
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        PassengerRequest request = new PassengerRequest(6, 9, Priority.LOW, RequestType.STANDARD);
        assertTrue(strategy.isCloser(first, second, request));
        Elevator selected = strategy.selectElevator(elevators, request);
        assertEquals(first, selected);
    }

    @Test
    public void testHighEfficiencyStrategyNotCloserBranch() {
        // 该测试验证isCloser在候选距离更远时会返回false
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 1, new NearestElevatorStrategy());
        FastElevator near = buildElevator(33, scheduler, 4, Direction.UP, ElevatorStatus.MOVING);
        FastElevator far = buildElevator(34, scheduler, 12, Direction.UP, ElevatorStatus.MOVING);
        elevators.add(near);
        elevators.add(far);
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        PassengerRequest request = new PassengerRequest(6, 8, Priority.MEDIUM, RequestType.STANDARD);
        assertFalse(strategy.isCloser(far, near, request));
    }

    @Test
    public void testEnergySavingStrategyPreferIdleThenClosest() {
        // 该测试验证节能策略优先选择空闲电梯，其次选择方向匹配且距离小于5层的电梯
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 1, new NearestElevatorStrategy());
        FastElevator idle = buildElevator(14, scheduler, 8, Direction.UP, ElevatorStatus.IDLE);
        FastElevator close = buildElevator(15, scheduler, 4, Direction.UP, ElevatorStatus.MOVING);
        FastElevator far = buildElevator(16, scheduler, 1, Direction.UP, ElevatorStatus.MOVING);
        elevators.add(idle);
        elevators.add(close);
        elevators.add(far);
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        PassengerRequest request = new PassengerRequest(5, 9, Priority.MEDIUM, RequestType.STANDARD);
        assertEquals(idle, strategy.selectElevator(elevators, request));
        idle.setStatus(ElevatorStatus.MOVING);
        assertEquals(close, strategy.selectElevator(elevators, request));
        far.setDirection(Direction.DOWN);
        assertNull(strategy.selectElevator(elevators, request));
    }

    @Test
    public void testPredictiveSchedulingStrategyCost() throws Exception {
        // 该测试验证预测策略对距离和载客因子的综合评估
        SystemConfig.getInstance().setMaxLoad(1);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(17, scheduler);
        elevators.add(elevator);
        PassengerRequest passenger = new PassengerRequest(3, 7, Priority.HIGH, RequestType.STANDARD);
        addPassenger(elevator, passenger);
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        PassengerRequest request = new PassengerRequest(5, 10, Priority.LOW, RequestType.STANDARD);
        double cost = strategy.calculatePredictedCost(elevator, request);
        assertEquals(5 + 10, cost, 0.001);
        Elevator selected = strategy.selectElevator(elevators, request);
        assertEquals(elevator, selected);
    }

    @Test
    public void testSchedulerSubmitRequestQueuesAndDispatch() throws Exception {
        // 该测试验证调度器高优先级请求进入独立队列并成功派发
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevatorList = new ArrayList<>();
        RecordingStrategy strategy = new RecordingStrategy();
        Scheduler scheduler = new Scheduler(elevatorList, 6, strategy);
        FastElevator assigned = new FastElevator(18, scheduler);
        elevatorList.add(assigned);
        strategy.setElevatorToReturn(assigned);
        PassengerRequest high = new PassengerRequest(2, 8, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(high);
        Queue<PassengerRequest> highQueue = getPrivateField(scheduler, "highPriorityQueue", Queue.class);
        assertEquals(1, highQueue.size());
        PassengerRequest normal = new PassengerRequest(3, 9, Priority.LOW, RequestType.STANDARD);
        scheduler.submitRequest(normal);
        List<PassengerRequest> pulled = scheduler.getRequestsAtFloor(3, Direction.UP);
        assertEquals(1, pulled.size());
        assertEquals(2, strategy.getRecordedRequests().size());
        scheduler.setDispatchStrategy(new EnergySavingStrategy());
        scheduler.dispatchElevator(normal);
        assertTrue(assigned.getDestinationSet().contains(3));
    }

    @Test
    public void testSchedulerSetDispatchStrategySwitch() throws Exception {
        // 该测试验证调度策略切换后新的策略返回结果会立即生效
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        RecordingStrategy initialStrategy = new RecordingStrategy();
        Scheduler scheduler = new Scheduler(elevators, 5, initialStrategy);
        FastElevator first = new FastElevator(19, scheduler);
        FastElevator second = new FastElevator(20, scheduler);
        elevators.add(first);
        elevators.add(second);
        initialStrategy.setElevatorToReturn(first);
        PassengerRequest request = new PassengerRequest(4, 6, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.dispatchElevator(request);
        assertTrue(first.getDestinationSet().contains(4));
        first.getDestinationSet().clear();
        scheduler.setDispatchStrategy((available, req) -> second);
        scheduler.dispatchElevator(request);
        assertTrue(second.getDestinationSet().contains(4));
    }

    @Test
    public void testSchedulerSingletonInitialization() throws Exception {
        // 该测试验证单例调度器的双重检验锁与默认实例
        cleanupSingletons();
        List<Elevator> elevators = new ArrayList<>();
        Scheduler created = Scheduler.getInstance(elevators, 4, new NearestElevatorStrategy());
        Scheduler reused = Scheduler.getInstance();
        assertSame(created, reused);
        Scheduler stillSame = Scheduler.getInstance(new ArrayList<>(), 9, new HighEfficiencyStrategy());
        assertSame(created, stillSame);
        Map<Integer, Floor> floors = getPrivateField(created, "floors", Map.class);
        assertEquals(4, floors.size());
    }

    @Test
    public void testSchedulerDispatchNoElevatorMessage() {
        // 该测试验证当无可用电梯时会打印提示信息
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        RecordingStrategy strategy = new RecordingStrategy();
        Scheduler scheduler = new Scheduler(elevators, 5, strategy);
        PassengerRequest request = new PassengerRequest(1, 3, Priority.LOW, RequestType.STANDARD);
        String output = captureOutput(() -> scheduler.dispatchElevator(request));
        assertTrue(output.contains("No available"));
    }

    @Test
    public void testSchedulerRedistributeRequestsAndEmergencyProtocol() throws Exception {
        // 该测试验证调度器在电梯故障和紧急方案中的处理逻辑
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevatorList = new ArrayList<>();
        RecordingStrategy strategy = new RecordingStrategy();
        Scheduler scheduler = new Scheduler(elevatorList, 6, strategy);
        FastElevator faulty = new FastElevator(21, scheduler);
        FastElevator standby = new FastElevator(22, scheduler);
        elevatorList.add(faulty);
        elevatorList.add(standby);
        strategy.setElevatorToReturn(standby);
        PassengerRequest first = new PassengerRequest(1, 9, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest second = new PassengerRequest(2, 10, Priority.MEDIUM, RequestType.STANDARD);
        addPassenger(faulty, first);
        addPassenger(faulty, second);
        addDestination(faulty, 9);
        addDestination(faulty, 10);
        scheduler.redistributeRequests(faulty);
        assertEquals(2, strategy.getRecordedRequests().size());
        assertTrue(faulty.getPassengerList().isEmpty());
        AtomicBoolean emergencyHandled = new AtomicBoolean(false);
        FastElevator emergencyElevator = new FastElevator(23, scheduler) {
            @Override
            public void handleEmergency() {
                emergencyHandled.set(true);
                super.handleEmergency();
            }
        };
        elevatorList.add(emergencyElevator);
        scheduler.executeEmergencyProtocol();
        assertTrue(emergencyHandled.get());
    }

    @Test
    public void testSchedulerUpdateHandlesDifferentEvents() {
        // 该测试验证观察者回调根据事件类型触发不同逻辑
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        SpyScheduler scheduler = new SpyScheduler(elevators, 5);
        FastElevator elevator = new FastElevator(24, scheduler);
        elevators.add(elevator);
        scheduler.update(elevator, new Event(EventType.ELEVATOR_FAULT, null));
        assertTrue(scheduler.redistributed);
        scheduler.update(elevator, new Event(EventType.EMERGENCY, null));
        assertTrue(scheduler.emergencyTriggered);
    }

    @Test
    public void testLogManagerRecordAndQuery() throws Exception {
        // 该测试验证日志记录接口及时间过滤条件
        LogManager logManager = LogManager.getInstance();
        long start = System.currentTimeMillis();
        logManager.recordElevatorEvent(1, "arrived");
        logManager.recordSchedulerEvent("dispatched");
        logManager.recordEvent("SecurityMonitor", "Handling emergency");
        long end = System.currentTimeMillis() + 1000;
        List<LogManager.SystemLog> schedulerLogs = logManager.queryLogs("Scheduler", start, end);
        assertEquals(1, schedulerLogs.size());
        assertEquals("dispatched", schedulerLogs.get(0).getMessage());
    }

    @Test
    public void testAnalyticsEnginePeakHoursAndReport() throws Exception {
        // 该测试验证分析引擎对报表、峰值判断及状态收集的功能
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        ElevatorStatusReport report = new ElevatorStatusReport(1, 5, Direction.UP, ElevatorStatus.MOVING, 2.5, 350, 5);
        engine.processStatusReport(report);
        engine.updateFloorPassengerCount(1, 30);
        engine.updateFloorPassengerCount(2, 25);
        assertTrue(engine.isPeakHours());
        engine.updateFloorPassengerCount(2, 10);
        assertFalse(engine.isPeakHours());
        AnalyticsEngine.Report performance = engine.generatePerformanceReport();
        assertTrue(performance.getTitle().contains("Performance"));
        assertTrue(performance.getGeneratedTime() > 0);
    }

    @Test
    public void testElevatorStatusReportToString() {
        // 该测试验证电梯状态报告的toString包含关键字段
        ElevatorStatusReport report = new ElevatorStatusReport(2, 9, Direction.DOWN, ElevatorStatus.STOPPED, 1.5, 200, 4);
        String summary = report.toString();
        assertTrue(summary.contains("elevatorId=2"));
        assertTrue(summary.contains("currentFloor=9"));
    }

    @Test
    public void testNotificationServiceChannelRouting() throws Exception {
        // 该测试验证不同通知类型只会触发支持的通道
        NotificationService service = NotificationService.getInstance();
        List<NotificationService.NotificationChannel> channels = getPrivateField(service, "channels", List.class);
        channels.clear();
        RecordingChannel emergencyChannel = new RecordingChannel(NotificationService.NotificationType.EMERGENCY);
        RecordingChannel allChannel = new RecordingChannel();
        channels.add(emergencyChannel);
        channels.add(allChannel);
        NotificationService.Notification emergency = new NotificationService.Notification(
                NotificationService.NotificationType.EMERGENCY,
                "fire",
                Arrays.asList("a@b.com"));
        service.sendNotification(emergency);
        NotificationService.Notification info = new NotificationService.Notification(
                NotificationService.NotificationType.INFORMATION,
                "info",
                Arrays.asList("c@d.com"));
        service.sendNotification(info);
        assertEquals(1, emergencyChannel.getReceived().size());
        assertEquals(2, allChannel.getReceived().size());
    }

    @Test
    public void testNotificationChannelsStandalone() {
        // 该测试验证默认短信与邮件通道的支持范围与输出
        NotificationService.SMSChannel sms = new NotificationService.SMSChannel();
        NotificationService.EmailChannel email = new NotificationService.EmailChannel();
        NotificationService.Notification emergency = new NotificationService.Notification(
                NotificationService.NotificationType.EMERGENCY,
                "evacuate",
                Arrays.asList("sms@a.com"));
        NotificationService.Notification info = new NotificationService.Notification(
                NotificationService.NotificationType.INFORMATION,
                "daily",
                Arrays.asList("mail@a.com"));
        assertTrue(sms.supports(NotificationService.NotificationType.EMERGENCY));
        assertFalse(sms.supports(NotificationService.NotificationType.INFORMATION));
        String smsOutput = captureOutput(() -> sms.send(emergency));
        assertTrue(smsOutput.contains("Sending SMS"));
        assertTrue(email.supports(NotificationService.NotificationType.INFORMATION));
        String mailOutput = captureOutput(() -> email.send(info));
        assertTrue(mailOutput.contains("Sending email notification"));
    }

    @Test
    public void testMaintenanceManagerSchedulingAndEventHandling() throws Exception {
        // 该测试验证维修管理器对故障任务的排队与记录处理
        MaintenanceManager manager = MaintenanceManager.getInstance();
        shutdownExecutorField(manager, "executorService");
        SystemConfig.getInstance().setMaxLoad(500);
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 1, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(25, scheduler);
        manager.scheduleMaintenance(elevator);
        Queue<MaintenanceManager.MaintenanceTask> queue = getPrivateField(manager, "taskQueue", Queue.class);
        assertEquals(1, queue.size());
        MaintenanceManager.MaintenanceTask task = queue.poll();
        assertEquals(25, task.getElevatorId());
        manager.performMaintenance(task);
        List<MaintenanceManager.MaintenanceRecord> records = getPrivateField(manager, "maintenanceRecords", List.class);
        assertFalse(records.isEmpty());
        EventBus.Event faultEvent = new EventBus.Event(EventType.ELEVATOR_FAULT, elevator);
        manager.onEvent(faultEvent);
        assertEquals(1, queue.size());
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerProcessTasksLoop() throws Exception {
        // 该测试验证processTasks循环能处理任务并在中断后退出
        TestMaintenanceManager manager = new TestMaintenanceManager();
        shutdownExecutorField(manager, "executorService");
        Queue<MaintenanceManager.MaintenanceTask> queue = getPrivateField(manager, "taskQueue", Queue.class);
        queue.add(new MaintenanceManager.MaintenanceTask(90, System.currentTimeMillis(), "loop"));
        Thread worker = new Thread(manager::processTasks);
        worker.start();
        assertTrue(manager.awaitProcessing());
        worker.interrupt();
        worker.join(2000);
        assertFalse(worker.isAlive());
    }

    @Test
    public void testSecurityMonitorHandleEmergencyFlow() throws Exception {
        // 该测试验证安防监控在收到紧急事件时的日志、通知及调度联动
        TestNotificationService notificationService = new TestNotificationService();
        setStaticField(NotificationService.class, "instance", notificationService);
        List<Elevator> elevators = new ArrayList<>();
        SpyScheduler scheduler = new SpyScheduler(elevators, 1);
        setStaticField(Scheduler.class, "instance", scheduler);
        SecurityMonitor monitor = SecurityMonitor.getInstance();
        shutdownExecutorField(monitor, "executorService");
        EventBus eventBus = EventBus.getInstance();
        eventBus.publish(new EventBus.Event(EventType.EMERGENCY, "FIRE"));
        List<SecurityMonitor.SecurityEvent> events = getPrivateField(monitor, "securityEvents", List.class);
        assertEquals(1, events.size());
        assertEquals("FIRE", events.get(0).getData());
        assertEquals(1, notificationService.getSentNotifications().size());
        assertTrue(notificationService.getSentNotifications().get(0).getMessage().contains("FIRE"));
        assertTrue(scheduler.emergencyTriggered);
        List<LogManager.SystemLog> securityLogs = LogManager.getInstance().queryLogs("SecurityMonitor", 0, Long.MAX_VALUE);
        assertFalse(securityLogs.isEmpty());
    }

    @Test
    public void testEventBusSubscribeAndPublish() {
        // 该测试验证事件总线只向订阅者推送对应类型事件
        EventBus eventBus = EventBus.getInstance();
        List<EventBus.Event> received = new ArrayList<>();
        eventBus.publish(new EventBus.Event(EventType.CONFIG_UPDATED, "none"));
        eventBus.subscribe(EventType.CONFIG_UPDATED, received::add);
        EventBus.Event event = new EventBus.Event(EventType.CONFIG_UPDATED, "CFG");
        eventBus.publish(event);
        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    public void testEventClassBeanProperties() {
        // 该测试验证事件实体的取值方法
        Event event = new Event(EventType.MAINTENANCE_REQUIRED, "payload");
        assertEquals(EventType.MAINTENANCE_REQUIRED, event.getType());
        assertEquals("payload", event.getData());
    }

    @Test
    public void testElevatorManagerRegistry() {
        // 该测试验证电梯管理器的注册和查询功能
        ElevatorManager manager = ElevatorManager.getInstance();
        SystemConfig.getInstance().setMaxLoad(500);
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 1, new NearestElevatorStrategy());
        FastElevator elevator = new FastElevator(30, scheduler);
        manager.registerElevator(elevator);
        assertEquals(elevator, manager.getElevatorById(30));
        Collection<Elevator> elevators = manager.getAllElevators();
        assertTrue(elevators.contains(elevator));
    }

    @Test
    public void testThreadPoolManagerSubmitAndShutdown() throws Exception {
        // 该测试验证线程池能够执行任务并在正常关闭时退出
        ThreadPoolManager manager = new ThreadPoolManager();
        CountDownLatch latch = new CountDownLatch(1);
        manager.submitTask(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        manager.shutdown();
    }

    @Test
    public void testThreadPoolManagerShutdownFallbackPath() throws Exception {
        // 该测试验证线程池在超时未结束时会执行立即关闭分支
        ThreadPoolManager manager = new ThreadPoolManager();
        Field execField = ThreadPoolManager.class.getDeclaredField("executorService");
        execField.setAccessible(true);
        ExecutorService original = (ExecutorService) execField.get(manager);
        original.shutdownNow();
        RejectingExecutorService stub = new RejectingExecutorService();
        setFinalField(manager, "executorService", stub);
        manager.shutdown();
        assertTrue(stub.shutdownCalled);
        assertTrue(stub.shutdownNowCalled);
    }

    @Test
    public void testThreadPoolManagerShutdownInterruptedPath() throws Exception {
        // 该测试验证线程池在awaitTermination抛出中断时的处理逻辑
        ThreadPoolManager manager = new ThreadPoolManager();
        Field execField = ThreadPoolManager.class.getDeclaredField("executorService");
        execField.setAccessible(true);
        ExecutorService original = (ExecutorService) execField.get(manager);
        original.shutdownNow();
        InterruptingExecutorService stub = new InterruptingExecutorService();
        setFinalField(manager, "executorService", stub);
        assertFalse(Thread.currentThread().isInterrupted());
        manager.shutdown();
        assertTrue(stub.shutdownCalled);
        assertTrue(stub.shutdownNowCalled);
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    // --------------------------------- Helper Classes ---------------------------------

    private static class FastElevator extends Elevator {
        private boolean emergencyHandled;

        public FastElevator(int id, Scheduler scheduler) {
            super(id, scheduler);
        }

        @Override
        public void openDoor() {
            setStatus(ElevatorStatus.STOPPED);
            unloadPassengers();
            loadPassengers();
        }

        @Override
        public void handleEmergency() {
            emergencyHandled = true;
            super.handleEmergency();
        }

        public boolean isEmergencyHandled() {
            return emergencyHandled;
        }
    }

    private static class StubScheduler extends Scheduler {
        private final Map<Integer, Map<Direction, List<PassengerRequest>>> requests = new HashMap<>();

        public StubScheduler(List<Elevator> elevatorList, int floorCount) {
            super(elevatorList, floorCount, new NearestElevatorStrategy());
        }

        public void presetRequests(int floor, Direction direction, List<PassengerRequest> passengerRequests) {
            requests.computeIfAbsent(floor, k -> new EnumMap<>(Direction.class)).put(direction, passengerRequests);
        }

        @Override
        public List<PassengerRequest> getRequestsAtFloor(int floorNumber, Direction direction) {
            Map<Direction, List<PassengerRequest>> perDir = requests.get(floorNumber);
            if (perDir != null && perDir.containsKey(direction)) {
                return new ArrayList<>(perDir.remove(direction));
            }
            return Collections.emptyList();
        }
    }

    private static class SpyScheduler extends Scheduler {
        private boolean redistributed;
        private boolean emergencyTriggered;

        public SpyScheduler(List<Elevator> elevatorList, int floorCount) {
            super(elevatorList, floorCount, new NearestElevatorStrategy());
        }

        public SpyScheduler(List<Elevator> elevatorList, int floorCount, DispatchStrategy strategy) {
            super(elevatorList, floorCount, strategy);
        }

        @Override
        public void redistributeRequests(Elevator faultyElevator) {
            redistributed = true;
        }

        @Override
        public void executeEmergencyProtocol() {
            emergencyTriggered = true;
        }
    }

    private static class TestMaintenanceManager extends MaintenanceManager {
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void performMaintenance(MaintenanceTask task) {
            super.performMaintenance(task);
            latch.countDown();
        }

        public boolean awaitProcessing() throws InterruptedException {
            return latch.await(2, TimeUnit.SECONDS);
        }
    }

    private static class RecordingStrategy implements DispatchStrategy {
        private final List<PassengerRequest> recordedRequests = new ArrayList<>();
        private Elevator elevatorToReturn;

        @Override
        public Elevator selectElevator(List<Elevator> elevators, PassengerRequest request) {
            recordedRequests.add(request);
            return elevatorToReturn;
        }

        public void setElevatorToReturn(Elevator elevator) {
            this.elevatorToReturn = elevator;
        }

        public List<PassengerRequest> getRecordedRequests() {
            return recordedRequests;
        }
    }

    private static class RecordingChannel implements NotificationService.NotificationChannel {
        private final Set<NotificationService.NotificationType> supported;
        private final List<NotificationService.Notification> received = new ArrayList<>();

        public RecordingChannel(NotificationService.NotificationType... supportedTypes) {
            this.supported = new HashSet<>(Arrays.asList(supportedTypes));
        }

        @Override
        public boolean supports(NotificationService.NotificationType type) {
            return supported.isEmpty() || supported.contains(type);
        }

        @Override
        public void send(NotificationService.Notification notification) {
            received.add(notification);
        }

        public List<NotificationService.Notification> getReceived() {
            return received;
        }
    }

    private static class TestNotificationService extends NotificationService {
        private final List<NotificationService.Notification> sentNotifications = new ArrayList<>();

        @Override
        public void sendNotification(NotificationService.Notification notification) {
            sentNotifications.add(notification);
        }

        public List<NotificationService.Notification> getSentNotifications() {
            return sentNotifications;
        }
    }

    private static class RejectingExecutorService extends AbstractExecutorService {
        private boolean shutdownCalled;
        private boolean shutdownNowCalled;

        @Override
        public void shutdown() {
            shutdownCalled = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdownNowCalled = true;
            return new ArrayList<>();
        }

        @Override
        public boolean isShutdown() {
            return shutdownCalled;
        }

        @Override
        public boolean isTerminated() {
            return shutdownNowCalled;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return false;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    private static class InterruptingExecutorService extends AbstractExecutorService {
        private boolean shutdownCalled;
        private boolean shutdownNowCalled;

        @Override
        public void shutdown() {
            shutdownCalled = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdownNowCalled = true;
            return new ArrayList<>();
        }

        @Override
        public boolean isShutdown() {
            return shutdownCalled;
        }

        @Override
        public boolean isTerminated() {
            return shutdownNowCalled;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            throw new InterruptedException("forced");
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }
}
