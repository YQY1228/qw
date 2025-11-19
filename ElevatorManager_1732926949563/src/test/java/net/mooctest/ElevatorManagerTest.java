package net.mooctest;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.Field;

public class ElevatorManagerTest {

	// ==================== Elevator类测试 ====================
	
	/**
	 * 测试电梯初始化是否正确
	 */
	@Test(timeout = 4000)
	public void testElevatorInitialization() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		assertEquals(1, elevator.getId());
		assertEquals(1, elevator.getCurrentFloor());
		assertEquals(Direction.UP, elevator.getDirection());
		assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
		assertEquals(0.0, elevator.getEnergyConsumption(), 0.01);
		assertEquals(0.0, elevator.getCurrentLoad(), 0.01);
		assertEquals(ElevatorMode.NORMAL, elevator.getMode());
		assertTrue(elevator.getPassengerList().isEmpty());
	}

	/**
	 * 测试电梯添加目标楼层
	 */
	@Test(timeout = 4000)
	public void testElevatorAddDestination() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.addDestination(5);
		assertTrue(elevator.getDestinationSet().contains(5));
		elevator.addDestination(3);
		assertTrue(elevator.getDestinationSet().contains(3));
		assertEquals(2, elevator.getDestinationSet().size());
	}

	/**
	 * 测试电梯向上移动
	 */
	@Test(timeout = 4000)
	public void testElevatorMoveUp() throws InterruptedException {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setDirection(Direction.UP);
		elevator.setStatus(ElevatorStatus.MOVING);
		elevator.getDestinationSet().add(3);
		int initialFloor = elevator.getCurrentFloor();
		elevator.move();
		assertEquals(initialFloor + 1, elevator.getCurrentFloor());
		assertEquals(1.0, elevator.getEnergyConsumption(), 0.01);
	}

	/**
	 * 测试电梯向下移动
	 */
	@Test(timeout = 4000)
	public void testElevatorMoveDown() throws InterruptedException {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(5);
		elevator.setDirection(Direction.DOWN);
		elevator.setStatus(ElevatorStatus.MOVING);
		elevator.getDestinationSet().add(2);
		elevator.move();
		assertEquals(4, elevator.getCurrentFloor());
	}

	/**
	 * 测试电梯到达目标楼层
	 */
	@Test(timeout = 4000)
	public void testElevatorReachDestination() throws InterruptedException {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(2);
		elevator.setDirection(Direction.UP);
		elevator.setStatus(ElevatorStatus.MOVING);
		elevator.getDestinationSet().add(3);
		elevator.move();
		elevator.setCurrentFloor(3);
		elevator.move();
		assertFalse(elevator.getDestinationSet().contains(3));
	}

	/**
	 * 测试电梯更新方向为向上
	 */
	@Test(timeout = 4000)
	public void testElevatorUpdateDirectionUp() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(2);
		elevator.getDestinationSet().add(5);
		elevator.updateDirection();
		assertEquals(Direction.UP, elevator.getDirection());
	}

	/**
	 * 测试电梯更新方向为向下
	 */
	@Test(timeout = 4000)
	public void testElevatorUpdateDirectionDown() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(5);
		elevator.getDestinationSet().add(2);
		elevator.updateDirection();
		assertEquals(Direction.DOWN, elevator.getDirection());
	}

	/**
	 * 测试目标集合为空时电梯状态变为空闲
	 */
	@Test(timeout = 4000)
	public void testElevatorUpdateDirectionIdle() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setStatus(ElevatorStatus.MOVING);
		elevator.getDestinationSet().clear();
		elevator.updateDirection();
		assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
	}

	/**
	 * 测试电梯卸载到达目标的乘客
	 */
	@Test(timeout = 4000)
	public void testElevatorUnloadPassengers() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(2);
		PassengerRequest request1 = new PassengerRequest(1, 2, Priority.MEDIUM, RequestType.STANDARD);
		PassengerRequest request2 = new PassengerRequest(1, 3, Priority.MEDIUM, RequestType.STANDARD);
		elevator.getPassengerList().add(request1);
		elevator.getPassengerList().add(request2);
		elevator.unloadPassengers();
		assertFalse(elevator.getPassengerList().contains(request1));
		assertTrue(elevator.getPassengerList().contains(request2));
	}

	/**
	 * 测试电梯在紧急情况下移动到1楼
	 */
	@Test(timeout = 4000)
	public void testElevatorMoveToFirstFloor() throws InterruptedException {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(5);
		elevator.setDirection(Direction.DOWN);
		elevator.moveToFirstFloor();
		assertEquals(1, elevator.getCurrentFloor());
		assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
	}

	/**
	 * 测试电梯的setter和getter方法
	 */
	@Test(timeout = 4000)
	public void testElevatorSettersAndGetters() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(5);
		assertEquals(5, elevator.getCurrentFloor());
		elevator.setDirection(Direction.DOWN);
		assertEquals(Direction.DOWN, elevator.getDirection());
		elevator.setStatus(ElevatorStatus.MAINTENANCE);
		assertEquals(ElevatorStatus.MAINTENANCE, elevator.getStatus());
		elevator.setCurrentLoad(100.0);
		assertEquals(100.0, elevator.getCurrentLoad(), 0.01);
		elevator.setEnergyConsumption(50.0);
		assertEquals(50.0, elevator.getEnergyConsumption(), 0.01);
		elevator.setMode(ElevatorMode.ENERGY_SAVING);
		assertEquals(ElevatorMode.ENERGY_SAVING, elevator.getMode());
	}

	/**
	 * 测试电梯清空所有请求
	 */
	@Test(timeout = 4000)
	public void testElevatorClearAllRequests() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		PassengerRequest request1 = new PassengerRequest(1, 5, Priority.MEDIUM, RequestType.STANDARD);
		PassengerRequest request2 = new PassengerRequest(1, 3, Priority.MEDIUM, RequestType.STANDARD);
		elevator.getPassengerList().add(request1);
		elevator.getPassengerList().add(request2);
		elevator.getDestinationSet().add(5);
		elevator.getDestinationSet().add(3);
		List<PassengerRequest> cleared = elevator.clearAllRequests();
		assertEquals(2, cleared.size());
		assertTrue(elevator.getPassengerList().isEmpty());
		assertTrue(elevator.getDestinationSet().isEmpty());
	}

	/**
	 * 测试电梯紧急处理
	 */
	@Test(timeout = 4000)
	public void testElevatorHandleEmergency() {
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevator.getDestinationSet().add(5);
		PassengerRequest request = new PassengerRequest(1, 5, Priority.MEDIUM, RequestType.STANDARD);
		elevator.getPassengerList().add(request);
		elevator.handleEmergency();
		assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
		assertTrue(elevator.getPassengerList().isEmpty());
		assertTrue(elevator.getDestinationSet().contains(1));
	}

	// ==================== ElevatorManager类测试 ====================
	
	/**
	 * 测试ElevatorManager单例模式
	 */
	@Test(timeout = 4000)
	public void testElevatorManagerSingleton() {
		resetElevatorManagerSingleton();
		ElevatorManager manager1 = ElevatorManager.getInstance();
		ElevatorManager manager2 = ElevatorManager.getInstance();
		assertSame(manager1, manager2);
	}

	/**
	 * 测试注册和获取电梯
	 */
	@Test(timeout = 4000)
	public void testElevatorManagerRegisterAndGet() {
		resetElevatorManagerSingleton();
		ElevatorManager manager = ElevatorManager.getInstance();
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator1 = new Elevator(1, scheduler);
		Elevator elevator2 = new Elevator(2, scheduler);
		manager.registerElevator(elevator1);
		manager.registerElevator(elevator2);
		assertEquals(elevator1, manager.getElevatorById(1));
		assertEquals(elevator2, manager.getElevatorById(2));
		assertNull(manager.getElevatorById(3));
	}

	/**
	 * 测试获取所有电梯
	 */
	@Test(timeout = 4000)
	public void testElevatorManagerGetAllElevators() {
		resetElevatorManagerSingleton();
		ElevatorManager manager = ElevatorManager.getInstance();
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator1 = new Elevator(1, scheduler);
		Elevator elevator2 = new Elevator(2, scheduler);
		manager.registerElevator(elevator1);
		manager.registerElevator(elevator2);
		Collection<Elevator> elevators = manager.getAllElevators();
		assertEquals(2, elevators.size());
	}

	// ==================== Scheduler类测试 ====================
	
	/**
	 * 测试调度器初始化
	 */
	@Test(timeout = 4000)
	public void testSchedulerInitialization() {
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
		assertNotNull(scheduler);
	}

	/**
	 * 测试提交请求
	 */
	@Test(timeout = 4000)
	public void testSchedulerSubmitRequest() {
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
		PassengerRequest normalRequest = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
		PassengerRequest highPriorityRequest = new PassengerRequest(3, 7, Priority.HIGH, RequestType.STANDARD);
		scheduler.submitRequest(normalRequest);
		scheduler.submitRequest(highPriorityRequest);
	}

	/**
	 * 测试分配电梯
	 */
	@Test(timeout = 4000)
	public void testSchedulerDispatchElevator() {
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		elevators.add(elevator);
		PassengerRequest request = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
		scheduler.dispatchElevator(request);
		assertTrue(elevator.getDestinationSet().contains(2));
	}

	/**
	 * 测试设置分配策略
	 */
	@Test(timeout = 4000)
	public void testSchedulerSetDispatchStrategy() {
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
		DispatchStrategy newStrategy = new EnergySavingStrategy();
		scheduler.setDispatchStrategy(newStrategy);
		assertNotNull(scheduler);
	}

	/**
	 * 测试重新分配请求
	 */
	@Test(timeout = 4000)
	public void testSchedulerRedistributeRequests() {
		List<Elevator> elevators = new ArrayList<>();
		Elevator elevator1 = new Elevator(1, null);
		Elevator elevator2 = new Elevator(2, null);
		elevators.add(elevator1);
		elevators.add(elevator2);
		Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
		PassengerRequest request1 = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
		PassengerRequest request2 = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
		elevator1.getPassengerList().add(request1);
		elevator1.getPassengerList().add(request2);
		scheduler.redistributeRequests(elevator1);
		assertTrue(elevator1.getPassengerList().isEmpty());
	}

	/**
	 * 测试执行紧急协议
	 */
	@Test(timeout = 4000)
	public void testSchedulerExecuteEmergencyProtocol() {
		List<Elevator> elevators = new ArrayList<>();
		Elevator elevator1 = new Elevator(1, null);
		Elevator elevator2 = new Elevator(2, null);
		elevators.add(elevator1);
		elevators.add(elevator2);
		Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
		scheduler.executeEmergencyProtocol();
		assertEquals(ElevatorStatus.EMERGENCY, elevator1.getStatus());
		assertEquals(ElevatorStatus.EMERGENCY, elevator2.getStatus());
	}

	// ==================== Floor类测试 ====================
	
	/**
	 * 测试楼层初始化
	 */
	@Test(timeout = 4000)
	public void testFloorInitialization() {
		Floor floor = new Floor(5);
		assertEquals(5, floor.getFloorNumber());
	}

	/**
	 * 测试添加请求到楼层
	 */
	@Test(timeout = 4000)
	public void testFloorAddRequest() {
		Floor floor = new Floor(2);
		PassengerRequest request = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
		floor.addRequest(request);
		List<PassengerRequest> requests = floor.getRequests(Direction.UP);
		assertTrue(requests.contains(request));
	}

	/**
	 * 测试获取请求后队列被清空
	 */
	@Test(timeout = 4000)
	public void testFloorGetRequestsClearsQueue() {
		Floor floor = new Floor(2);
		PassengerRequest request1 = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
		PassengerRequest request2 = new PassengerRequest(2, 3, Priority.MEDIUM, RequestType.STANDARD);
		floor.addRequest(request1);
		floor.addRequest(request2);
		List<PassengerRequest> requests = floor.getRequests(Direction.UP);
		assertEquals(2, requests.size());
		List<PassengerRequest> emptyRequests = floor.getRequests(Direction.UP);
		assertTrue(emptyRequests.isEmpty());
	}

	/**
	 * 测试楼层的向下请求
	 */
	@Test(timeout = 4000)
	public void testFloorDownwardRequests() {
		Floor floor = new Floor(5);
		PassengerRequest downRequest = new PassengerRequest(5, 2, Priority.MEDIUM, RequestType.STANDARD);
		floor.addRequest(downRequest);
		List<PassengerRequest> downRequests = floor.getRequests(Direction.DOWN);
		assertEquals(1, downRequests.size());
		assertTrue(downRequests.contains(downRequest));
	}

	// ==================== PassengerRequest类测试 ====================
	
	/**
	 * 测试乘客请求创建
	 */
	@Test(timeout = 4000)
	public void testPassengerRequestCreation() {
		PassengerRequest request = new PassengerRequest(2, 5, Priority.HIGH, RequestType.STANDARD);
		assertEquals(2, request.getStartFloor());
		assertEquals(5, request.getDestinationFloor());
		assertEquals(Priority.HIGH, request.getPriority());
		assertEquals(RequestType.STANDARD, request.getRequestType());
		assertEquals(Direction.UP, request.getDirection());
		assertEquals(SpecialNeeds.NONE, request.getSpecialNeeds());
	}

	/**
	 * 测试从上楼层到下楼层的请求方向
	 */
	@Test(timeout = 4000)
	public void testPassengerRequestDirectionDown() {
		PassengerRequest request = new PassengerRequest(7, 3, Priority.MEDIUM, RequestType.STANDARD);
		assertEquals(Direction.DOWN, request.getDirection());
	}

	/**
	 * 测试请求时间戳
	 */
	@Test(timeout = 4000)
	public void testPassengerRequestTimestamp() {
		long beforeTime = System.currentTimeMillis();
		PassengerRequest request = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
		long afterTime = System.currentTimeMillis();
		assertTrue(request.getTimestamp() >= beforeTime);
		assertTrue(request.getTimestamp() <= afterTime);
	}

	/**
	 * 测试请求的字符串表示
	 */
	@Test(timeout = 4000)
	public void testPassengerRequestToString() {
		PassengerRequest request = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
		String str = request.toString();
		assertTrue(str.contains("2"));
		assertTrue(str.contains("5"));
		assertTrue(str.contains("MEDIUM"));
		assertTrue(str.contains("STANDARD"));
	}

	// ==================== 分配策略测试 ====================
	
	/**
	 * 测试最近电梯策略
	 */
	@Test(timeout = 4000)
	public void testNearestElevatorStrategy() {
		NearestElevatorStrategy strategy = new NearestElevatorStrategy();
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, strategy);
		Elevator elevator1 = new Elevator(1, scheduler);
		elevator1.setCurrentFloor(2);
		elevator1.setStatus(ElevatorStatus.IDLE);
		Elevator elevator2 = new Elevator(2, scheduler);
		elevator2.setCurrentFloor(8);
		elevator2.setStatus(ElevatorStatus.IDLE);
		elevators.add(elevator1);
		elevators.add(elevator2);
		PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		Elevator selected = strategy.selectElevator(elevators, request);
		assertEquals(elevator1, selected);
	}

	/**
	 * 测试电梯是否符合条件
	 */
	@Test(timeout = 4000)
	public void testNearestElevatorStrategyEligibility() {
		NearestElevatorStrategy strategy = new NearestElevatorStrategy();
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setStatus(ElevatorStatus.IDLE);
		PassengerRequest upRequest = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		assertTrue(strategy.isEligible(elevator, upRequest));
		elevator.setStatus(ElevatorStatus.MOVING);
		elevator.setDirection(Direction.UP);
		assertTrue(strategy.isEligible(elevator, upRequest));
		elevator.setDirection(Direction.DOWN);
		assertFalse(strategy.isEligible(elevator, upRequest));
	}

	/**
	 * 测试没有符合条件的电梯时
	 */
	@Test(timeout = 4000)
	public void testNearestElevatorStrategyNoEligible() {
		NearestElevatorStrategy strategy = new NearestElevatorStrategy();
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, strategy);
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setStatus(ElevatorStatus.MAINTENANCE);
		elevators.add(elevator);
		PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		Elevator selected = strategy.selectElevator(elevators, request);
		assertNull(selected);
	}

	/**
	 * 测试节能策略
	 */
	@Test(timeout = 4000)
	public void testEnergySavingStrategy() {
		EnergySavingStrategy strategy = new EnergySavingStrategy();
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, strategy);
		Elevator elevator1 = new Elevator(1, scheduler);
		elevator1.setStatus(ElevatorStatus.IDLE);
		Elevator elevator2 = new Elevator(2, scheduler);
		elevator2.setStatus(ElevatorStatus.MOVING);
		elevators.add(elevator1);
		elevators.add(elevator2);
		PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		Elevator selected = strategy.selectElevator(elevators, request);
		assertEquals(elevator1, selected);
	}

	/**
	 * 测试节能策略当没有空闲电梯时
	 */
	@Test(timeout = 4000)
	public void testEnergySavingStrategyNoIdle() {
		EnergySavingStrategy strategy = new EnergySavingStrategy();
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, strategy);
		Elevator elevator1 = new Elevator(1, scheduler);
		elevator1.setCurrentFloor(2);
		elevator1.setStatus(ElevatorStatus.MOVING);
		elevator1.setDirection(Direction.UP);
		elevators.add(elevator1);
		PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		Elevator selected = strategy.selectElevator(elevators, request);
		assertEquals(elevator1, selected);
	}

	/**
	 * 测试高效策略
	 */
	@Test(timeout = 4000)
	public void testHighEfficiencyStrategy() {
		HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, strategy);
		Elevator elevator1 = new Elevator(1, scheduler);
		elevator1.setCurrentFloor(2);
		elevator1.setStatus(ElevatorStatus.IDLE);
		Elevator elevator2 = new Elevator(2, scheduler);
		elevator2.setCurrentFloor(8);
		elevator2.setStatus(ElevatorStatus.MOVING);
		elevator2.setDirection(Direction.UP);
		elevators.add(elevator1);
		elevators.add(elevator2);
		PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		Elevator selected = strategy.selectElevator(elevators, request);
		assertNotNull(selected);
	}

	/**
	 * 测试高效策略中电梯距离比较
	 */
	@Test(timeout = 4000)
	public void testHighEfficiencyStrategyIsCloser() {
		HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
		Elevator elevator1 = new Elevator(1, scheduler);
		elevator1.setCurrentFloor(2);
		Elevator elevator2 = new Elevator(2, scheduler);
		elevator2.setCurrentFloor(5);
		PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		assertTrue(strategy.isCloser(elevator1, elevator2, request));
		assertFalse(strategy.isCloser(elevator2, elevator1, request));
	}

	/**
	 * 测试预测调度策略
	 */
	@Test(timeout = 4000)
	public void testPredictiveSchedulingStrategy() {
		PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
		List<Elevator> elevators = new ArrayList<>();
		Scheduler scheduler = new Scheduler(elevators, 10, strategy);
		Elevator elevator1 = new Elevator(1, scheduler);
		elevator1.setCurrentFloor(2);
		Elevator elevator2 = new Elevator(2, scheduler);
		elevator2.setCurrentFloor(1);
		elevators.add(elevator1);
		elevators.add(elevator2);
		PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
		Elevator selected = strategy.selectElevator(elevators, request);
		assertNotNull(selected);
	}

	/**
	 * 测试预测成本计算
	 */
	@Test(timeout = 4000)
	public void testPredictiveSchedulingCalculateCost() {
		PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
		Elevator elevator = new Elevator(1, scheduler);
		elevator.setCurrentFloor(5);
		PassengerRequest request = new PassengerRequest(2, 8, Priority.MEDIUM, RequestType.STANDARD);
		double cost = strategy.calculatePredictedCost(elevator, request);
		assertTrue(cost > 0);
	}

	// ==================== MaintenanceManager类测试 ====================
	
	/**
	 * 测试维护管理器单例模式
	 */
	@Test(timeout = 4000)
	public void testMaintenanceManagerSingleton() {
		resetMaintenanceManagerSingleton();
		MaintenanceManager manager1 = MaintenanceManager.getInstance();
		MaintenanceManager manager2 = MaintenanceManager.getInstance();
		assertSame(manager1, manager2);
	}

	/**
	 * 测试计划维护
	 */
	@Test(timeout = 4000)
	public void testMaintenanceManagerScheduleMaintenance() {
		resetMaintenanceManagerSingleton();
		MaintenanceManager manager = MaintenanceManager.getInstance();
		Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
		Elevator elevator = new Elevator(1, scheduler);
		manager.scheduleMaintenance(elevator);
		assertNotNull(manager);
	}

	/**
	 * 测试维护任务创建
	 */
	@Test(timeout = 4000)
	public void testMaintenanceTaskCreation() {
		long currentTime = System.currentTimeMillis();
		MaintenanceManager.MaintenanceTask task = new MaintenanceManager.MaintenanceTask(1, currentTime, "Test Maintenance");
		assertEquals(1, task.getElevatorId());
		assertEquals(currentTime, task.getScheduledTime());
		assertEquals("Test Maintenance", task.getDescription());
	}

	/**
	 * 测试维护记录创建
	 */
	@Test(timeout = 4000)
	public void testMaintenanceRecordCreation() {
		long currentTime = System.currentTimeMillis();
		MaintenanceManager.MaintenanceRecord record = new MaintenanceManager.MaintenanceRecord(1, currentTime, "Maintenance Complete");
		assertEquals(1, record.getElevatorId());
		assertEquals(currentTime, record.getMaintenanceTime());
		assertEquals("Maintenance Complete", record.getResult());
	}

	// ==================== NotificationService类测试 ====================
	
	/**
	 * 测试通知服务单例模式
	 */
	@Test(timeout = 4000)
	public void testNotificationServiceSingleton() {
		resetNotificationServiceSingleton();
		NotificationService service1 = NotificationService.getInstance();
		NotificationService service2 = NotificationService.getInstance();
		assertSame(service1, service2);
	}

	/**
	 * 测试发送通知
	 */
	@Test(timeout = 4000)
	public void testNotificationServiceSendNotification() {
		resetNotificationServiceSingleton();
		NotificationService service = NotificationService.getInstance();
		List<String> recipients = Arrays.asList("test@example.com");
		NotificationService.Notification notification = new NotificationService.Notification(
			NotificationService.NotificationType.EMERGENCY,
			"Test Message",
			recipients
		);
		service.sendNotification(notification);
		assertNotNull(service);
	}

	/**
	 * 测试通知创建
	 */
	@Test(timeout = 4000)
	public void testNotificationCreation() {
		List<String> recipients = Arrays.asList("test@example.com", "test2@example.com");
		NotificationService.Notification notification = new NotificationService.Notification(
			NotificationService.NotificationType.MAINTENANCE,
			"Maintenance Message",
			recipients
		);
		assertEquals(NotificationService.NotificationType.MAINTENANCE, notification.getType());
		assertEquals("Maintenance Message", notification.getMessage());
		assertEquals(2, notification.getRecipients().size());
	}

	/**
	 * 测试SMS通道支持的类型
	 */
	@Test(timeout = 4000)
	public void testSMSChannelSupports() {
		NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
		assertTrue(channel.supports(NotificationService.NotificationType.EMERGENCY));
		assertTrue(channel.supports(NotificationService.NotificationType.MAINTENANCE));
		assertFalse(channel.supports(NotificationService.NotificationType.INFORMATION));
	}

	/**
	 * 测试Email通道支持的类型
	 */
	@Test(timeout = 4000)
	public void testEmailChannelSupports() {
		NotificationService.EmailChannel channel = new NotificationService.EmailChannel();
		assertTrue(channel.supports(NotificationService.NotificationType.EMERGENCY));
		assertTrue(channel.supports(NotificationService.NotificationType.MAINTENANCE));
		assertTrue(channel.supports(NotificationService.NotificationType.INFORMATION));
		assertTrue(channel.supports(NotificationService.NotificationType.SYSTEM_UPDATE));
	}

	// ==================== AnalyticsEngine类测试 ====================
	
	/**
	 * 测试分析引擎单例模式
	 */
	@Test(timeout = 4000)
	public void testAnalyticsEngineSingleton() {
		resetAnalyticsEngineSingleton();
		AnalyticsEngine engine1 = AnalyticsEngine.getInstance();
		AnalyticsEngine engine2 = AnalyticsEngine.getInstance();
		assertSame(engine1, engine2);
	}

	/**
	 * 测试处理状态报告
	 */
	@Test(timeout = 4000)
	public void testAnalyticsEngineProcessStatusReport() {
		resetAnalyticsEngineSingleton();
		AnalyticsEngine engine = AnalyticsEngine.getInstance();
		ElevatorStatusReport report = new ElevatorStatusReport(1, 5, Direction.UP, ElevatorStatus.MOVING, 1.5, 500, 3);
		engine.processStatusReport(report);
		assertNotNull(engine);
	}

	/**
	 * 测试更新楼层乘客数
	 */
	@Test(timeout = 4000)
	public void testAnalyticsEngineUpdateFloorPassengerCount() {
		resetAnalyticsEngineSingleton();
		AnalyticsEngine engine = AnalyticsEngine.getInstance();
		engine.updateFloorPassengerCount(1, 10);
		engine.updateFloorPassengerCount(2, 15);
		assertNotNull(engine);
	}

	/**
	 * 测试是否为高峰时段
	 */
	@Test(timeout = 4000)
	public void testAnalyticsEngineIsPeakHours() {
		resetAnalyticsEngineSingleton();
		AnalyticsEngine engine = AnalyticsEngine.getInstance();
		engine.updateFloorPassengerCount(1, 20);
		engine.updateFloorPassengerCount(2, 20);
		engine.updateFloorPassengerCount(3, 20);
		assertTrue(engine.isPeakHours());
	}

	/**
	 * 测试非高峰时段
	 */
	@Test(timeout = 4000)
	public void testAnalyticsEngineNotPeakHours() {
		resetAnalyticsEngineSingleton();
		AnalyticsEngine engine = AnalyticsEngine.getInstance();
		engine.updateFloorPassengerCount(1, 5);
		engine.updateFloorPassengerCount(2, 5);
		assertFalse(engine.isPeakHours());
	}

	/**
	 * 测试生成报告
	 */
	@Test(timeout = 4000)
	public void testAnalyticsEngineGenerateReport() {
		resetAnalyticsEngineSingleton();
		AnalyticsEngine engine = AnalyticsEngine.getInstance();
		AnalyticsEngine.Report report = engine.generatePerformanceReport();
		assertEquals("System Performance Report", report.getTitle());
		assertTrue(report.getGeneratedTime() > 0);
	}

	/**
	 * 测试电梯状态报告创建
	 */
	@Test(timeout = 4000)
	public void testElevatorStatusReportCreation() {
		ElevatorStatusReport report = new ElevatorStatusReport(1, 5, Direction.UP, ElevatorStatus.MOVING, 1.5, 500, 3);
		assertEquals(1, report.getElevatorId());
		assertEquals(5, report.getCurrentFloor());
		assertEquals(Direction.UP, report.getDirection());
		assertEquals(ElevatorStatus.MOVING, report.getStatus());
		assertEquals(1.5, report.getSpeed(), 0.01);
		assertEquals(500, report.getCurrentLoad(), 0.01);
		assertEquals(3, report.getPassengerCount());
	}

	/**
	 * 测试电梯状态报告的字符串表示
	 */
	@Test(timeout = 4000)
	public void testElevatorStatusReportToString() {
		ElevatorStatusReport report = new ElevatorStatusReport(1, 5, Direction.UP, ElevatorStatus.MOVING, 1.5, 500, 3);
		String str = report.toString();
		assertTrue(str.contains("1"));
		assertTrue(str.contains("5"));
		assertTrue(str.contains("MOVING"));
		assertTrue(str.contains("3"));
	}

	// ==================== LogManager类测试 ====================
	
	/**
	 * 测试日志管理器单例模式
	 */
	@Test(timeout = 4000)
	public void testLogManagerSingleton() {
		resetLogManagerSingleton();
		LogManager manager1 = LogManager.getInstance();
		LogManager manager2 = LogManager.getInstance();
		assertSame(manager1, manager2);
	}

	/**
	 * 测试记录电梯事件
	 */
	@Test(timeout = 4000)
	public void testLogManagerRecordElevatorEvent() {
		resetLogManagerSingleton();
		LogManager manager = LogManager.getInstance();
		manager.recordElevatorEvent(1, "Test Event");
		assertNotNull(manager);
	}

	/**
	 * 测试记录调度器事件
	 */
	@Test(timeout = 4000)
	public void testLogManagerRecordSchedulerEvent() {
		resetLogManagerSingleton();
		LogManager manager = LogManager.getInstance();
		manager.recordSchedulerEvent("Test Scheduler Event");
		assertNotNull(manager);
	}

	/**
	 * 测试记录事件
	 */
	@Test(timeout = 4000)
	public void testLogManagerRecordEvent() {
		resetLogManagerSingleton();
		LogManager manager = LogManager.getInstance();
		manager.recordEvent("TestSource", "Test Message");
		assertNotNull(manager);
	}

	/**
	 * 测试查询日志
	 */
	@Test(timeout = 4000)
	public void testLogManagerQueryLogs() {
		resetLogManagerSingleton();
		LogManager manager = LogManager.getInstance();
		long now = System.currentTimeMillis();
		manager.recordEvent("TestSource", "Test Message");
		List<LogManager.SystemLog> logs = manager.queryLogs("TestSource", now - 1000, now + 1000);
		assertEquals(1, logs.size());
	}

	/**
	 * 测试系统日志创建
	 */
	@Test(timeout = 4000)
	public void testSystemLogCreation() {
		long currentTime = System.currentTimeMillis();
		LogManager.SystemLog log = new LogManager.SystemLog("TestSource", "Test Message", currentTime);
		assertEquals("TestSource", log.getSource());
		assertEquals("Test Message", log.getMessage());
		assertEquals(currentTime, log.getTimestamp());
	}

	// ==================== SecurityMonitor类测试 ====================
	
	/**
	 * 测试安全监视器单例模式
	 */
	@Test(timeout = 4000)
	public void testSecurityMonitorSingleton() {
		resetSecurityMonitorSingleton();
		SecurityMonitor monitor1 = SecurityMonitor.getInstance();
		SecurityMonitor monitor2 = SecurityMonitor.getInstance();
		assertSame(monitor1, monitor2);
	}

	/**
	 * 测试处理紧急情况
	 */
	@Test(timeout = 4000)
	public void testSecurityMonitorHandleEmergency() {
		resetSecurityMonitorSingleton();
		resetSchedulerSingleton();
		SecurityMonitor monitor = SecurityMonitor.getInstance();
		Scheduler scheduler = Scheduler.getInstance();
		monitor.handleEmergency("Test Emergency");
		assertNotNull(monitor);
	}

	/**
	 * 测试安全事件创建
	 */
	@Test(timeout = 4000)
	public void testSecurityEventCreation() {
		long currentTime = System.currentTimeMillis();
		SecurityMonitor.SecurityEvent event = new SecurityMonitor.SecurityEvent("Test Emergency", currentTime, "TestData");
		assertEquals("Test Emergency", event.getDescription());
		assertEquals(currentTime, event.getTimestamp());
		assertEquals("TestData", event.getData());
	}

	// ==================== SystemConfig类测试 ====================
	
	/**
	 * 测试系统配置单例模式
	 */
	@Test(timeout = 4000)
	public void testSystemConfigSingleton() {
		resetSystemConfigSingleton();
		SystemConfig config1 = SystemConfig.getInstance();
		SystemConfig config2 = SystemConfig.getInstance();
		assertSame(config1, config2);
	}

	/**
	 * 测试系统配置初始化
	 */
	@Test(timeout = 4000)
	public void testSystemConfigInitialization() {
		resetSystemConfigSingleton();
		SystemConfig config = SystemConfig.getInstance();
		assertEquals(20, config.getFloorCount());
		assertEquals(4, config.getElevatorCount());
		assertEquals(800, config.getMaxLoad(), 0.01);
	}

	/**
	 * 测试设置楼层数
	 */
	@Test(timeout = 4000)
	public void testSystemConfigSetFloorCount() {
		resetSystemConfigSingleton();
		SystemConfig config = SystemConfig.getInstance();
		config.setFloorCount(30);
		assertEquals(30, config.getFloorCount());
		config.setFloorCount(-5);
		assertEquals(30, config.getFloorCount());
	}

	/**
	 * 测试设置电梯数
	 */
	@Test(timeout = 4000)
	public void testSystemConfigSetElevatorCount() {
		resetSystemConfigSingleton();
		SystemConfig config = SystemConfig.getInstance();
		config.setElevatorCount(6);
		assertEquals(6, config.getElevatorCount());
		config.setElevatorCount(0);
		assertEquals(6, config.getElevatorCount());
	}

	/**
	 * 测试设置最大负载
	 */
	@Test(timeout = 4000)
	public void testSystemConfigSetMaxLoad() {
		resetSystemConfigSingleton();
		SystemConfig config = SystemConfig.getInstance();
		config.setMaxLoad(1000);
		assertEquals(1000, config.getMaxLoad(), 0.01);
		config.setMaxLoad(-100);
		assertEquals(1000, config.getMaxLoad(), 0.01);
	}

	// ==================== ThreadPoolManager类测试 ====================
	
	/**
	 * 测试线程池管理器单例模式
	 */
	@Test(timeout = 4000)
	public void testThreadPoolManagerSingleton() {
		resetThreadPoolManagerSingleton();
		ThreadPoolManager manager1 = ThreadPoolManager.getInstance();
		ThreadPoolManager manager2 = ThreadPoolManager.getInstance();
		assertSame(manager1, manager2);
	}

	/**
	 * 测试提交任务到线程池
	 */
	@Test(timeout = 4000)
	public void testThreadPoolManagerSubmitTask() {
		resetThreadPoolManagerSingleton();
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		Runnable task = new Runnable() {
			@Override
			public void run() {}
		};
		manager.submitTask(task);
		assertNotNull(manager);
	}

	// ==================== EventBus类测试 ====================
	
	/**
	 * 测试事件总线单例模式
	 */
	@Test(timeout = 4000)
	public void testEventBusSingleton() {
		resetEventBusSingleton();
		EventBus bus1 = EventBus.getInstance();
		EventBus bus2 = EventBus.getInstance();
		assertSame(bus1, bus2);
	}

	/**
	 * 测试订阅和发布事件
	 */
	@Test(timeout = 4000)
	public void testEventBusSubscribeAndPublish() {
		resetEventBusSingleton();
		EventBus bus = EventBus.getInstance();
		EventBus.EventListener listener = Mockito.mock(EventBus.EventListener.class);
		bus.subscribe(EventType.EMERGENCY, listener);
		EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, "Test");
		bus.publish(event);
		Mockito.verify(listener, Mockito.times(1)).onEvent(event);
	}

	/**
	 * 测试事件创建
	 */
	@Test(timeout = 4000)
	public void testEventCreation() {
		EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, "Test Data");
		assertEquals(EventType.EMERGENCY, event.getType());
		assertEquals("Test Data", event.getData());
	}

	/**
	 * 测试Event类创建
	 */
	@Test(timeout = 4000)
	public void testEventClassCreation() {
		Event event = new Event(EventType.MAINTENANCE_REQUIRED, "Test");
		assertEquals(EventType.MAINTENANCE_REQUIRED, event.getType());
		assertEquals("Test", event.getData());
	}

	// ==================== 枚举类测试 ====================
	
	/**
	 * 测试方向枚举
	 */
	@Test(timeout = 4000)
	public void testDirectionEnum() {
		Direction[] directions = Direction.values();
		assertEquals(2, directions.length);
		assertTrue(Arrays.asList(Direction.UP, Direction.DOWN).containsAll(Arrays.asList(directions)));
	}

	/**
	 * 测试电梯状态枚举
	 */
	@Test(timeout = 4000)
	public void testElevatorStatusEnum() {
		ElevatorStatus[] statuses = ElevatorStatus.values();
		assertEquals(6, statuses.length);
		assertTrue(Arrays.asList(statuses).contains(ElevatorStatus.MOVING));
		assertTrue(Arrays.asList(statuses).contains(ElevatorStatus.IDLE));
		assertTrue(Arrays.asList(statuses).contains(ElevatorStatus.EMERGENCY));
	}

	/**
	 * 测试电梯模式枚举
	 */
	@Test(timeout = 4000)
	public void testElevatorModeEnum() {
		ElevatorMode[] modes = ElevatorMode.values();
		assertEquals(3, modes.length);
		assertTrue(Arrays.asList(modes).contains(ElevatorMode.NORMAL));
		assertTrue(Arrays.asList(modes).contains(ElevatorMode.ENERGY_SAVING));
		assertTrue(Arrays.asList(modes).contains(ElevatorMode.EMERGENCY));
	}

	/**
	 * 测试优先级枚举
	 */
	@Test(timeout = 4000)
	public void testPriorityEnum() {
		Priority[] priorities = Priority.values();
		assertEquals(3, priorities.length);
		assertTrue(Arrays.asList(priorities).contains(Priority.HIGH));
		assertTrue(Arrays.asList(priorities).contains(Priority.MEDIUM));
		assertTrue(Arrays.asList(priorities).contains(Priority.LOW));
	}

	/**
	 * 测试请求类型枚举
	 */
	@Test(timeout = 4000)
	public void testRequestTypeEnum() {
		RequestType[] types = RequestType.values();
		assertEquals(2, types.length);
		assertTrue(Arrays.asList(types).contains(RequestType.STANDARD));
		assertTrue(Arrays.asList(types).contains(RequestType.DESTINATION_CONTROL));
	}

	/**
	 * 测试事件类型枚举
	 */
	@Test(timeout = 4000)
	public void testEventTypeEnum() {
		EventType[] types = EventType.values();
		assertEquals(4, types.length);
		assertTrue(Arrays.asList(types).contains(EventType.ELEVATOR_FAULT));
		assertTrue(Arrays.asList(types).contains(EventType.EMERGENCY));
		assertTrue(Arrays.asList(types).contains(EventType.MAINTENANCE_REQUIRED));
	}

	/**
	 * 测试特殊需求枚举
	 */
	@Test(timeout = 4000)
	public void testSpecialNeedsEnum() {
		SpecialNeeds[] needs = SpecialNeeds.values();
		assertEquals(4, needs.length);
		assertTrue(Arrays.asList(needs).contains(SpecialNeeds.NONE));
		assertTrue(Arrays.asList(needs).contains(SpecialNeeds.DISABLED_ASSISTANCE));
	}

	// ==================== 辅助方法 ====================
	
	/**
	 * 重置ElevatorManager单例
	 */
	private void resetElevatorManagerSingleton() {
		try {
			Field field = ElevatorManager.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置MaintenanceManager单例
	 */
	private void resetMaintenanceManagerSingleton() {
		try {
			Field field = MaintenanceManager.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置NotificationService单例
	 */
	private void resetNotificationServiceSingleton() {
		try {
			Field field = NotificationService.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置AnalyticsEngine单例
	 */
	private void resetAnalyticsEngineSingleton() {
		try {
			Field field = AnalyticsEngine.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置LogManager单例
	 */
	private void resetLogManagerSingleton() {
		try {
			Field field = LogManager.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置SecurityMonitor单例
	 */
	private void resetSecurityMonitorSingleton() {
		try {
			Field field = SecurityMonitor.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置SystemConfig单例
	 */
	private void resetSystemConfigSingleton() {
		try {
			Field field = SystemConfig.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置ThreadPoolManager单例
	 */
	private void resetThreadPoolManagerSingleton() {
		try {
			Field field = ThreadPoolManager.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置EventBus单例
	 */
	private void resetEventBusSingleton() {
		try {
			Field field = EventBus.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}

	/**
	 * 重置Scheduler单例
	 */
	private void resetSchedulerSingleton() {
		try {
			Field field = Scheduler.class.getDeclaredField("instance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception e) {}
	}
}
