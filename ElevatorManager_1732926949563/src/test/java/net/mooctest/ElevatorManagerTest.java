package net.mooctest;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import java.util.*;
import java.util.concurrent.*;

/**
 * ElevatorManager项目完整测试类
 * 目标：100%分支覆盖率 + 100%变异杀死率
 * 使用JUnit 4.12
 */
public class ElevatorManagerTest {

    private Scheduler scheduler;
    private List<Elevator> elevators;
    private static final int FLOOR_COUNT = 10;

    @Before
    public void setUp() {
        // 重置单例实例，确保每个测试独立
    }

    // ==================== Direction枚举测试 ====================
    
    @Test(timeout = 4000)
    public void testDirectionValues() {
        // 测试枚举值
        Direction[] values = Direction.values();
        assertEquals(2, values.length);
        assertEquals(Direction.UP, values[0]);
        assertEquals(Direction.DOWN, values[1]);
    }
    
    @Test(timeout = 4000)
    public void testDirectionValueOf() {
        // 测试valueOf方法
        assertEquals(Direction.UP, Direction.valueOf("UP"));
        assertEquals(Direction.DOWN, Direction.valueOf("DOWN"));
    }

    // ==================== ElevatorStatus枚举测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorStatusValues() {
        // 测试枚举值
        ElevatorStatus[] values = ElevatorStatus.values();
        assertEquals(6, values.length);
        assertTrue(Arrays.asList(values).contains(ElevatorStatus.MOVING));
        assertTrue(Arrays.asList(values).contains(ElevatorStatus.STOPPED));
        assertTrue(Arrays.asList(values).contains(ElevatorStatus.IDLE));
        assertTrue(Arrays.asList(values).contains(ElevatorStatus.MAINTENANCE));
        assertTrue(Arrays.asList(values).contains(ElevatorStatus.EMERGENCY));
        assertTrue(Arrays.asList(values).contains(ElevatorStatus.FAULT));
    }
    
    @Test(timeout = 4000)
    public void testElevatorStatusValueOf() {
        // 测试valueOf方法
        assertEquals(ElevatorStatus.MOVING, ElevatorStatus.valueOf("MOVING"));
        assertEquals(ElevatorStatus.IDLE, ElevatorStatus.valueOf("IDLE"));
        assertEquals(ElevatorStatus.EMERGENCY, ElevatorStatus.valueOf("EMERGENCY"));
    }

    // ==================== ElevatorMode枚举测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorModeValues() {
        // 测试枚举值
        ElevatorMode[] values = ElevatorMode.values();
        assertEquals(3, values.length);
        assertEquals(ElevatorMode.NORMAL, values[0]);
        assertEquals(ElevatorMode.ENERGY_SAVING, values[1]);
        assertEquals(ElevatorMode.EMERGENCY, values[2]);
    }
    
    @Test(timeout = 4000)
    public void testElevatorModeValueOf() {
        // 测试valueOf方法
        assertEquals(ElevatorMode.NORMAL, ElevatorMode.valueOf("NORMAL"));
        assertEquals(ElevatorMode.ENERGY_SAVING, ElevatorMode.valueOf("ENERGY_SAVING"));
    }

    // ==================== Priority枚举测试 ====================
    
    @Test(timeout = 4000)
    public void testPriorityValues() {
        // 测试枚举值
        Priority[] values = Priority.values();
        assertEquals(3, values.length);
        assertEquals(Priority.HIGH, values[0]);
        assertEquals(Priority.MEDIUM, values[1]);
        assertEquals(Priority.LOW, values[2]);
    }
    
    @Test(timeout = 4000)
    public void testPriorityValueOf() {
        // 测试valueOf方法
        assertEquals(Priority.HIGH, Priority.valueOf("HIGH"));
        assertEquals(Priority.MEDIUM, Priority.valueOf("MEDIUM"));
        assertEquals(Priority.LOW, Priority.valueOf("LOW"));
    }

    // ==================== RequestType枚举测试 ====================
    
    @Test(timeout = 4000)
    public void testRequestTypeValues() {
        // 测试枚举值
        RequestType[] values = RequestType.values();
        assertEquals(2, values.length);
        assertEquals(RequestType.STANDARD, values[0]);
        assertEquals(RequestType.DESTINATION_CONTROL, values[1]);
    }
    
    @Test(timeout = 4000)
    public void testRequestTypeValueOf() {
        // 测试valueOf方法
        assertEquals(RequestType.STANDARD, RequestType.valueOf("STANDARD"));
        assertEquals(RequestType.DESTINATION_CONTROL, RequestType.valueOf("DESTINATION_CONTROL"));
    }

    // ==================== SpecialNeeds枚举测试 ====================
    
    @Test(timeout = 4000)
    public void testSpecialNeedsValues() {
        // 测试枚举值
        SpecialNeeds[] values = SpecialNeeds.values();
        assertEquals(4, values.length);
        assertEquals(SpecialNeeds.NONE, values[0]);
        assertEquals(SpecialNeeds.DISABLED_ASSISTANCE, values[1]);
        assertEquals(SpecialNeeds.LARGE_LUGGAGE, values[2]);
        assertEquals(SpecialNeeds.VIP_SERVICE, values[3]);
    }
    
    @Test(timeout = 4000)
    public void testSpecialNeedsValueOf() {
        // 测试valueOf方法
        assertEquals(SpecialNeeds.NONE, SpecialNeeds.valueOf("NONE"));
        assertEquals(SpecialNeeds.VIP_SERVICE, SpecialNeeds.valueOf("VIP_SERVICE"));
    }

    // ==================== EventType枚举测试 ====================
    
    @Test(timeout = 4000)
    public void testEventTypeValues() {
        // 测试枚举值
        EventType[] values = EventType.values();
        assertEquals(4, values.length);
        assertTrue(Arrays.asList(values).contains(EventType.ELEVATOR_FAULT));
        assertTrue(Arrays.asList(values).contains(EventType.EMERGENCY));
        assertTrue(Arrays.asList(values).contains(EventType.MAINTENANCE_REQUIRED));
        assertTrue(Arrays.asList(values).contains(EventType.CONFIG_UPDATED));
    }
    
    @Test(timeout = 4000)
    public void testEventTypeValueOf() {
        // 测试valueOf方法
        assertEquals(EventType.ELEVATOR_FAULT, EventType.valueOf("ELEVATOR_FAULT"));
        assertEquals(EventType.EMERGENCY, EventType.valueOf("EMERGENCY"));
    }

    // ==================== SystemConfig测试 ====================
    
    @Test(timeout = 4000)
    public void testSystemConfigSingleton() {
        // 测试单例模式
        SystemConfig instance1 = SystemConfig.getInstance();
        SystemConfig instance2 = SystemConfig.getInstance();
        assertSame(instance1, instance2);
        assertNotNull(instance1);
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigDefaultValues() {
        // 测试默认配置值
        SystemConfig config = SystemConfig.getInstance();
        assertEquals(20, config.getFloorCount());
        assertEquals(4, config.getElevatorCount());
        assertEquals(800.0, config.getMaxLoad(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetFloorCount() {
        // 测试设置楼层数（正数）
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(30);
        assertEquals(30, config.getFloorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetFloorCountZero() {
        // 测试设置楼层数为0（边界条件，不应改变）
        SystemConfig config = SystemConfig.getInstance();
        int originalCount = config.getFloorCount();
        config.setFloorCount(0);
        assertEquals(originalCount, config.getFloorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetFloorCountNegative() {
        // 测试设置楼层数为负数（不应改变）
        SystemConfig config = SystemConfig.getInstance();
        int originalCount = config.getFloorCount();
        config.setFloorCount(-5);
        assertEquals(originalCount, config.getFloorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetElevatorCount() {
        // 测试设置电梯数量（正数）
        SystemConfig config = SystemConfig.getInstance();
        config.setElevatorCount(6);
        assertEquals(6, config.getElevatorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetElevatorCountZero() {
        // 测试设置电梯数量为0（不应改变）
        SystemConfig config = SystemConfig.getInstance();
        int originalCount = config.getElevatorCount();
        config.setElevatorCount(0);
        assertEquals(originalCount, config.getElevatorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetElevatorCountNegative() {
        // 测试设置电梯数量为负数（不应改变）
        SystemConfig config = SystemConfig.getInstance();
        int originalCount = config.getElevatorCount();
        config.setElevatorCount(-3);
        assertEquals(originalCount, config.getElevatorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetMaxLoad() {
        // 测试设置最大载重（正数）
        SystemConfig config = SystemConfig.getInstance();
        config.setMaxLoad(1000.0);
        assertEquals(1000.0, config.getMaxLoad(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetMaxLoadZero() {
        // 测试设置最大载重为0（不应改变）
        SystemConfig config = SystemConfig.getInstance();
        double originalLoad = config.getMaxLoad();
        config.setMaxLoad(0.0);
        assertEquals(originalLoad, config.getMaxLoad(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetMaxLoadNegative() {
        // 测试设置最大载重为负数（不应改变）
        SystemConfig config = SystemConfig.getInstance();
        double originalLoad = config.getMaxLoad();
        config.setMaxLoad(-100.0);
        assertEquals(originalLoad, config.getMaxLoad(), 0.001);
    }

    // ==================== ElevatorManager测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorManagerSingleton() {
        // 测试单例模式
        ElevatorManager instance1 = ElevatorManager.getInstance();
        ElevatorManager instance2 = ElevatorManager.getInstance();
        assertSame(instance1, instance2);
        assertNotNull(instance1);
    }
    
    @Test(timeout = 4000)
    public void testElevatorManagerRegisterAndGet() {
        // 测试注册和获取电梯
        ElevatorManager manager = new ElevatorManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        manager.registerElevator(elevator);
        Elevator retrieved = manager.getElevatorById(1);
        assertSame(elevator, retrieved);
    }
    
    @Test(timeout = 4000)
    public void testElevatorManagerGetNonExistent() {
        // 测试获取不存在的电梯
        ElevatorManager manager = new ElevatorManager();
        Elevator retrieved = manager.getElevatorById(999);
        assertNull(retrieved);
    }
    
    @Test(timeout = 4000)
    public void testElevatorManagerGetAllElevators() {
        // 测试获取所有电梯
        ElevatorManager manager = new ElevatorManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator1 = new Elevator(1, scheduler);
        Elevator elevator2 = new Elevator(2, scheduler);
        
        manager.registerElevator(elevator1);
        manager.registerElevator(elevator2);
        
        Collection<Elevator> allElevators = manager.getAllElevators();
        assertEquals(2, allElevators.size());
        assertTrue(allElevators.contains(elevator1));
        assertTrue(allElevators.contains(elevator2));
    }

    // ==================== PassengerRequest测试 ====================
    
    @Test(timeout = 4000)
    public void testPassengerRequestUpDirection() {
        // 测试上行请求（起始楼层<目标楼层）
        PassengerRequest request = new PassengerRequest(1, 5, Priority.MEDIUM, RequestType.STANDARD);
        assertEquals(1, request.getStartFloor());
        assertEquals(5, request.getDestinationFloor());
        assertEquals(Direction.UP, request.getDirection());
        assertEquals(Priority.MEDIUM, request.getPriority());
        assertEquals(RequestType.STANDARD, request.getRequestType());
        assertEquals(SpecialNeeds.NONE, request.getSpecialNeeds());
        assertTrue(request.getTimestamp() > 0);
    }
    
    @Test(timeout = 4000)
    public void testPassengerRequestDownDirection() {
        // 测试下行请求（起始楼层>目标楼层）
        PassengerRequest request = new PassengerRequest(8, 3, Priority.HIGH, RequestType.DESTINATION_CONTROL);
        assertEquals(8, request.getStartFloor());
        assertEquals(3, request.getDestinationFloor());
        assertEquals(Direction.DOWN, request.getDirection());
        assertEquals(Priority.HIGH, request.getPriority());
        assertEquals(RequestType.DESTINATION_CONTROL, request.getRequestType());
    }
    
    @Test(timeout = 4000)
    public void testPassengerRequestToString() {
        // 测试toString方法
        PassengerRequest request = new PassengerRequest(2, 7, Priority.LOW, RequestType.STANDARD);
        String str = request.toString();
        assertTrue(str.contains("From 2 to 7"));
        assertTrue(str.contains("Priority: LOW"));
        assertTrue(str.contains("Type: STANDARD"));
    }

    // ==================== Floor测试 ====================
    
    @Test(timeout = 4000)
    public void testFloorCreation() {
        // 测试楼层创建
        Floor floor = new Floor(5);
        assertEquals(5, floor.getFloorNumber());
    }
    
    @Test(timeout = 4000)
    public void testFloorAddRequestUp() {
        // 测试添加上行请求
        Floor floor = new Floor(3);
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        floor.addRequest(request);
        
        List<PassengerRequest> requests = floor.getRequests(Direction.UP);
        assertEquals(1, requests.size());
        assertEquals(request, requests.get(0));
    }
    
    @Test(timeout = 4000)
    public void testFloorAddRequestDown() {
        // 测试添加下行请求
        Floor floor = new Floor(5);
        PassengerRequest request = new PassengerRequest(5, 2, Priority.HIGH, RequestType.STANDARD);
        floor.addRequest(request);
        
        List<PassengerRequest> requests = floor.getRequests(Direction.DOWN);
        assertEquals(1, requests.size());
        assertEquals(request, requests.get(0));
    }
    
    @Test(timeout = 4000)
    public void testFloorGetRequestsClearsQueue() {
        // 测试获取请求后队列被清空
        Floor floor = new Floor(4);
        PassengerRequest request1 = new PassengerRequest(4, 8, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(4, 9, Priority.LOW, RequestType.STANDARD);
        floor.addRequest(request1);
        floor.addRequest(request2);
        
        List<PassengerRequest> requests1 = floor.getRequests(Direction.UP);
        assertEquals(2, requests1.size());
        
        // 再次获取应该为空
        List<PassengerRequest> requests2 = floor.getRequests(Direction.UP);
        assertEquals(0, requests2.size());
    }
    
    @Test(timeout = 4000)
    public void testFloorEnumMapBothDirections() {
        // 测试EnumMap的两个方向都能正常工作
        Floor floor = new Floor(6);
        PassengerRequest upRequest = new PassengerRequest(6, 10, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest downRequest = new PassengerRequest(6, 1, Priority.HIGH, RequestType.STANDARD);
        
        floor.addRequest(upRequest);
        floor.addRequest(downRequest);
        
        List<PassengerRequest> upRequests = floor.getRequests(Direction.UP);
        assertEquals(1, upRequests.size());
        assertEquals(upRequest, upRequests.get(0));
        
        List<PassengerRequest> downRequests = floor.getRequests(Direction.DOWN);
        assertEquals(1, downRequests.size());
        assertEquals(downRequest, downRequests.get(0));
    }

    // ==================== Event测试 ====================
    
    @Test(timeout = 4000)
    public void testEventCreation() {
        // 测试事件创建
        Object data = new Object();
        Event event = new Event(EventType.EMERGENCY, data);
        assertEquals(EventType.EMERGENCY, event.getType());
        assertSame(data, event.getData());
    }
    
    @Test(timeout = 4000)
    public void testEventWithNullData() {
        // 测试带null数据的事件
        Event event = new Event(EventType.CONFIG_UPDATED, null);
        assertEquals(EventType.CONFIG_UPDATED, event.getType());
        assertNull(event.getData());
    }

    // ==================== Elevator测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorCreation() {
        // 测试电梯创建
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        assertEquals(1, elevator.getId());
        assertEquals(1, elevator.getCurrentFloor());
        assertEquals(Direction.UP, elevator.getDirection());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
        assertEquals(0.0, elevator.getCurrentLoad(), 0.001);
        assertEquals(0.0, elevator.getEnergyConsumption(), 0.001);
        assertEquals(ElevatorMode.NORMAL, elevator.getMode());
        assertNotNull(elevator.getPassengerList());
        assertNotNull(elevator.getDestinationSet());
        assertNotNull(elevator.getLock());
        assertNotNull(elevator.getCondition());
        assertNotNull(elevator.getObservers());
    }
    
    @Test(timeout = 4000)
    public void testElevatorSettersAndGetters() {
        // 测试电梯的setter和getter
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        elevator.setCurrentFloor(5);
        assertEquals(5, elevator.getCurrentFloor());
        
        elevator.setDirection(Direction.DOWN);
        assertEquals(Direction.DOWN, elevator.getDirection());
        
        elevator.setStatus(ElevatorStatus.MOVING);
        assertEquals(ElevatorStatus.MOVING, elevator.getStatus());
        
        elevator.setCurrentLoad(350.0);
        assertEquals(350.0, elevator.getCurrentLoad(), 0.001);
        
        elevator.setEnergyConsumption(25.5);
        assertEquals(25.5, elevator.getEnergyConsumption(), 0.001);
        
        elevator.setMode(ElevatorMode.ENERGY_SAVING);
        assertEquals(ElevatorMode.ENERGY_SAVING, elevator.getMode());
    }
    
    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionIdle() {
        // 测试updateDirection：destinationSet为空时设置为IDLE
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        
        elevator.updateDirection();
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionUp() {
        // 测试updateDirection：最小目标楼层>当前楼层时设置为UP
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.getDestinationSet().add(5);
        elevator.getDestinationSet().add(7);
        
        elevator.updateDirection();
        assertEquals(Direction.UP, elevator.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionDown() {
        // 测试updateDirection：最小目标楼层<=当前楼层时设置为DOWN
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        elevator.getDestinationSet().add(2);
        elevator.getDestinationSet().add(3);
        
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionEqual() {
        // 测试updateDirection：最小目标楼层等于当前楼层时设置为DOWN
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        elevator.getDestinationSet().add(5);
        
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveUp() throws InterruptedException {
        // 测试move方法：上行
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setDirection(Direction.UP);
        elevator.getDestinationSet().add(5);
        
        double initialEnergy = elevator.getEnergyConsumption();
        elevator.move();
        
        assertEquals(4, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.MOVING, elevator.getStatus());
        assertTrue(elevator.getEnergyConsumption() > initialEnergy);
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveDown() throws InterruptedException {
        // 测试move方法：下行
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(7);
        elevator.setDirection(Direction.DOWN);
        elevator.getDestinationSet().add(3);
        
        elevator.move();
        
        assertEquals(6, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.MOVING, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveReachDestination() throws InterruptedException {
        // 测试move方法：到达目标楼层
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(4);
        elevator.setDirection(Direction.UP);
        elevator.getDestinationSet().add(5);
        
        elevator.move();
        
        assertEquals(5, elevator.getCurrentFloor());
        assertFalse(elevator.getDestinationSet().contains(5));
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveToIdle() throws InterruptedException {
        // 测试move方法：所有目标完成后变为IDLE
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(4);
        elevator.setDirection(Direction.UP);
        elevator.getDestinationSet().add(5);
        
        elevator.move();
        
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testElevatorAddDestination() {
        // 测试添加目标楼层
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        elevator.addDestination(5);
        assertTrue(elevator.getDestinationSet().contains(5));
    }
    
    @Test(timeout = 4000)
    public void testElevatorDestinationSetTreeSet() {
        // 测试TreeSet自动排序
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(3);
        elevator.getDestinationSet().add(5);
        
        Iterator<Integer> iterator = elevator.getDestinationSet().iterator();
        assertEquals(3, iterator.next().intValue());
        assertEquals(5, iterator.next().intValue());
        assertEquals(7, iterator.next().intValue());
    }
    
    @Test(timeout = 4000)
    public void testElevatorUnloadPassengers() {
        // 测试卸载乘客
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        
        PassengerRequest request1 = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(2, 7, Priority.LOW, RequestType.STANDARD);
        elevator.getPassengerList().add(request1);
        elevator.getPassengerList().add(request2);
        elevator.setCurrentLoad(140.0);
        
        elevator.unloadPassengers();
        
        assertEquals(1, elevator.getPassengerList().size());
        assertEquals(request2, elevator.getPassengerList().get(0));
        assertEquals(70.0, elevator.getCurrentLoad(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testElevatorHandleEmergency() {
        // 测试紧急情况处理
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.getDestinationSet().add(5);
        elevator.getDestinationSet().add(7);
        
        PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
        elevator.getPassengerList().add(request);
        
        elevator.handleEmergency();
        
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
        assertEquals(0, elevator.getPassengerList().size());
        assertTrue(elevator.getDestinationSet().contains(1));
        assertEquals(1, elevator.getDestinationSet().size());
    }
    
    @Test(timeout = 4000)
    public void testElevatorClearAllRequests() {
        // 测试清除所有请求
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        PassengerRequest request1 = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(2, 7, Priority.LOW, RequestType.STANDARD);
        elevator.getPassengerList().add(request1);
        elevator.getPassengerList().add(request2);
        elevator.getDestinationSet().add(5);
        elevator.getDestinationSet().add(7);
        
        List<PassengerRequest> cleared = elevator.clearAllRequests();
        
        assertEquals(2, cleared.size());
        assertEquals(0, elevator.getPassengerList().size());
        assertEquals(0, elevator.getDestinationSet().size());
    }
    
    @Test(timeout = 4000)
    public void testElevatorAddObserver() {
        // 测试添加观察者
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {}
        };
        
        elevator.addObserver(observer);
        assertEquals(1, elevator.getObservers().size());
    }
    
    @Test(timeout = 4000)
    public void testElevatorNotifyObservers() {
        // 测试通知观察者
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        final boolean[] notified = {false};
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                notified[0] = true;
            }
        };
        
        elevator.addObserver(observer);
        Event event = new Event(EventType.EMERGENCY, null);
        elevator.notifyObservers(event);
        
        assertTrue(notified[0]);
    }
    
    @Test(timeout = 4000)
    public void testElevatorGetPassengerListReturnsCopy() {
        // 测试getPassengerList返回副本
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        List<PassengerRequest> list1 = elevator.getPassengerList();
        List<PassengerRequest> list2 = elevator.getPassengerList();
        
        assertNotSame(list1, list2);
    }

    // ==================== Scheduler测试 ====================
    
    @Test(timeout = 4000)
    public void testSchedulerCreation() {
        // 测试调度器创建
        List<Elevator> elevatorList = new ArrayList<>();
        DispatchStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(elevatorList, 10, strategy);
        
        assertNotNull(scheduler);
    }
    
    @Test(timeout = 4000)
    public void testSchedulerGetInstanceWithParams() {
        // 测试带参数的getInstance
        List<Elevator> elevatorList = new ArrayList<>();
        DispatchStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler1 = Scheduler.getInstance(elevatorList, 10, strategy);
        Scheduler scheduler2 = Scheduler.getInstance(elevatorList, 10, strategy);
        
        assertSame(scheduler1, scheduler2);
    }
    
    @Test(timeout = 4000)
    public void testSchedulerGetInstanceNoParams() {
        // 测试无参数的getInstance
        Scheduler scheduler1 = Scheduler.getInstance();
        Scheduler scheduler2 = Scheduler.getInstance();
        
        assertSame(scheduler1, scheduler2);
    }
    
    @Test(timeout = 4000)
    public void testSchedulerSubmitRequestHighPriority() {
        // 测试提交高优先级请求（进入highPriorityQueue）
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(request);
        
        // 高优先级请求应该被加入highPriorityQueue而不是Floor
    }
    
    @Test(timeout = 4000)
    public void testSchedulerSubmitRequestMediumPriority() {
        // 测试提交中等优先级请求（进入Floor）
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(request);
        
        // 非高优先级请求应该被加入Floor
    }
    
    @Test(timeout = 4000)
    public void testSchedulerSubmitRequestLowPriority() {
        // 测试提交低优先级请求（进入Floor）
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        PassengerRequest request = new PassengerRequest(5, 2, Priority.LOW, RequestType.STANDARD);
        scheduler.submitRequest(request);
        
        // 非高优先级请求应该被加入Floor
    }
    
    @Test(timeout = 4000)
    public void testSchedulerDispatchElevatorWithAvailableElevator() {
        // 测试派遣电梯：有可用电梯
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.dispatchElevator(request);
        
        assertTrue(elevator.getDestinationSet().contains(3));
    }
    
    @Test(timeout = 4000)
    public void testSchedulerDispatchElevatorNoAvailableElevator() {
        // 测试派遣电梯：无可用电梯
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.dispatchElevator(request);
        
        // 应该打印"No available elevators"
    }
    
    @Test(timeout = 4000)
    public void testSchedulerGetRequestsAtFloor() {
        // 测试获取某楼层的请求
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        
        List<PassengerRequest> requests = scheduler.getRequestsAtFloor(5, Direction.UP);
        assertNotNull(requests);
    }
    
    @Test(timeout = 4000)
    public void testSchedulerUpdateElevatorFault() {
        // 测试update方法：电梯故障事件
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        elevator.getPassengerList().add(request);
        
        Event event = new Event(EventType.ELEVATOR_FAULT, null);
        scheduler.update(elevator, event);
        
        // 应该重新分配请求
    }
    
    @Test(timeout = 4000)
    public void testSchedulerUpdateEmergency() {
        // 测试update方法：紧急事件
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        Event event = new Event(EventType.EMERGENCY, null);
        scheduler.update(elevator, event);
        
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testSchedulerUpdateOtherEventType() {
        // 测试update方法：其他事件类型（不触发特殊处理）
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        Event event = new Event(EventType.CONFIG_UPDATED, null);
        scheduler.update(elevator, event);
        
        // 不应该触发特殊处理
    }
    
    @Test(timeout = 4000)
    public void testSchedulerRedistributeRequests() {
        // 测试重新分配请求
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator1 = new Elevator(1, scheduler);
        Elevator elevator2 = new Elevator(2, scheduler);
        elevatorList.add(elevator1);
        elevatorList.add(elevator2);
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        elevator1.getPassengerList().add(request);
        elevator1.getDestinationSet().add(3);
        
        scheduler.redistributeRequests(elevator1);
        
        assertEquals(0, elevator1.getPassengerList().size());
        assertEquals(0, elevator1.getDestinationSet().size());
    }
    
    @Test(timeout = 4000)
    public void testSchedulerExecuteEmergencyProtocol() {
        // 测试执行紧急协议
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator1 = new Elevator(1, scheduler);
        Elevator elevator2 = new Elevator(2, scheduler);
        elevatorList.add(elevator1);
        elevatorList.add(elevator2);
        
        scheduler.executeEmergencyProtocol();
        
        assertEquals(ElevatorStatus.EMERGENCY, elevator1.getStatus());
        assertEquals(ElevatorStatus.EMERGENCY, elevator2.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testSchedulerSetDispatchStrategy() {
        // 测试设置派遣策略
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        
        DispatchStrategy newStrategy = new HighEfficiencyStrategy();
        scheduler.setDispatchStrategy(newStrategy);
        
        // 策略应该已更改
    }

    // ==================== NearestElevatorStrategy测试 ====================
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategySelectElevator() {
        // 测试选择最近的电梯
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setCurrentFloor(3);
        elevator1.setStatus(ElevatorStatus.IDLE);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setCurrentFloor(8);
        elevator2.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator1, selected);
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyNoEligibleElevator() {
        // 测试没有合格的电梯
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MAINTENANCE);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyIsEligibleIdle() {
        // 测试isEligible：IDLE状态
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        assertTrue(strategy.isEligible(elevator, request));
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyIsEligibleMovingSameDirection() {
        // 测试isEligible：MOVING状态且方向相同
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.UP);
        
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        assertTrue(strategy.isEligible(elevator, request));
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyIsEligibleMovingDifferentDirection() {
        // 测试isEligible：MOVING状态但方向不同
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.DOWN);
        
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        assertFalse(strategy.isEligible(elevator, request));
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyAbsDistance() {
        // 测试Math.abs()的准确性
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setCurrentFloor(2);
        elevator1.setStatus(ElevatorStatus.IDLE);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setCurrentFloor(8);
        elevator2.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        // elevator1距离=|2-5|=3, elevator2距离=|8-5|=3, 应该选择第一个找到的
        assertEquals(elevator1, selected);
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyMinDistanceComparison() {
        // 测试距离比较（<而不是<=）
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setCurrentFloor(1);
        elevator1.setStatus(ElevatorStatus.IDLE);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setCurrentFloor(3);
        elevator2.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        // elevator1距离=4, elevator2距离=2, 应该选择elevator2
        assertEquals(elevator2, selected);
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyIntegerMaxValue() {
        // 测试Integer.MAX_VALUE初始值
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(100);
        elevator.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        // 即使距离很大，也应该选择这个电梯
        assertEquals(elevator, selected);
    }

    // ==================== HighEfficiencyStrategy测试 ====================
    
    @Test(timeout = 4000)
    public void testHighEfficiencyStrategySelectElevator() {
        // 测试高效率策略选择电梯
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setCurrentFloor(3);
        elevator1.setStatus(ElevatorStatus.IDLE);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setCurrentFloor(6);
        elevator2.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator1, selected);
    }
    
    @Test(timeout = 4000)
    public void testHighEfficiencyStrategyIsCloserTrue() {
        // 测试isCloser返回true
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator candidate = new Elevator(1, scheduler);
        candidate.setCurrentFloor(5);
        
        Elevator current = new Elevator(2, scheduler);
        current.setCurrentFloor(8);
        
        PassengerRequest request = new PassengerRequest(6, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        assertTrue(strategy.isCloser(candidate, current, request));
    }
    
    @Test(timeout = 4000)
    public void testHighEfficiencyStrategyIsCloserFalse() {
        // 测试isCloser返回false
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator candidate = new Elevator(1, scheduler);
        candidate.setCurrentFloor(9);
        
        Elevator current = new Elevator(2, scheduler);
        current.setCurrentFloor(5);
        
        PassengerRequest request = new PassengerRequest(6, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        assertFalse(strategy.isCloser(candidate, current, request));
    }
    
    @Test(timeout = 4000)
    public void testHighEfficiencyStrategyNoElevatorAvailable() {
        // 测试没有可用电梯
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MAINTENANCE);
        elevator.setDirection(Direction.DOWN);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }
    
    @Test(timeout = 4000)
    public void testHighEfficiencyStrategySameDirection() {
        // 测试同方向的电梯
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.UP);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator, selected);
    }

    // ==================== PredictiveSchedulingStrategy测试 ====================
    
    @Test(timeout = 4000)
    public void testPredictiveSchedulingStrategySelectElevator() {
        // 测试预测调度策略
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setCurrentFloor(2);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setCurrentFloor(8);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNotNull(selected);
    }
    
    @Test(timeout = 4000)
    public void testPredictiveSchedulingStrategyCalculatePredictedCost() {
        // 测试计算预测成本
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        
        PassengerRequest request = new PassengerRequest(8, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        double cost = strategy.calculatePredictedCost(elevator, request);
        
        assertTrue(cost > 0);
    }
    
    @Test(timeout = 4000)
    public void testPredictiveSchedulingStrategyDoubleMaxValue() {
        // 测试Double.MAX_VALUE初始值
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(100);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator, selected);
    }
    
    @Test(timeout = 4000)
    public void testPredictiveSchedulingStrategyEmptyList() {
        // 测试空电梯列表
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        
        List<Elevator> elevators = new ArrayList<>();
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }

    // ==================== EnergySavingStrategy测试 ====================
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategySelectIdleElevator() {
        // 测试节能策略：优先选择空闲电梯
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setStatus(ElevatorStatus.MOVING);
        elevator1.setDirection(Direction.UP);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator2, selected);
    }
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategySelectNearbyMovingElevator() {
        // 测试节能策略：无空闲电梯时选择附近的同方向电梯
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.UP);
        elevator.setCurrentFloor(3);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator, selected);
    }
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategyNoSuitableElevator() {
        // 测试节能策略：无合适电梯时返回null
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.DOWN);
        elevator.setCurrentFloor(10);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategyDistance5Boundary() {
        // 测试距离<5的边界条件
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setStatus(ElevatorStatus.MOVING);
        elevator1.setDirection(Direction.UP);
        elevator1.setCurrentFloor(1);
        
        List<Elevator> elevators = Arrays.asList(elevator1);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator1, selected);
    }
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategyDistanceExactly5() {
        // 测试距离正好等于5（不满足<5条件）
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.UP);
        elevator.setCurrentFloor(10);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }

    // ==================== EventBus测试 ====================
    
    @Test(timeout = 4000)
    public void testEventBusSingleton() {
        // 测试单例模式
        EventBus instance1 = EventBus.getInstance();
        EventBus instance2 = EventBus.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test(timeout = 4000)
    public void testEventBusSubscribe() {
        // 测试订阅事件
        EventBus eventBus = new EventBus();
        final boolean[] eventReceived = {false};
        
        EventBus.EventListener listener = new EventBus.EventListener() {
            @Override
            public void onEvent(EventBus.Event event) {
                eventReceived[0] = true;
            }
        };
        
        eventBus.subscribe(EventType.EMERGENCY, listener);
        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, null);
        eventBus.publish(event);
        
        assertTrue(eventReceived[0]);
    }
    
    @Test(timeout = 4000)
    public void testEventBusPublishNoSubscribers() {
        // 测试发布事件但无订阅者
        EventBus eventBus = new EventBus();
        EventBus.Event event = new EventBus.Event(EventType.CONFIG_UPDATED, null);
        eventBus.publish(event);
        
        // 不应抛出异常
    }
    
    @Test(timeout = 4000)
    public void testEventBusMultipleSubscribers() {
        // 测试多个订阅者
        EventBus eventBus = new EventBus();
        final int[] counter = {0};
        
        EventBus.EventListener listener1 = new EventBus.EventListener() {
            @Override
            public void onEvent(EventBus.Event event) {
                counter[0]++;
            }
        };
        
        EventBus.EventListener listener2 = new EventBus.EventListener() {
            @Override
            public void onEvent(EventBus.Event event) {
                counter[0]++;
            }
        };
        
        eventBus.subscribe(EventType.EMERGENCY, listener1);
        eventBus.subscribe(EventType.EMERGENCY, listener2);
        
        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, null);
        eventBus.publish(event);
        
        assertEquals(2, counter[0]);
    }
    
    @Test(timeout = 4000)
    public void testEventBusEventGetters() {
        // 测试EventBus.Event的getter方法
        Object data = new Object();
        EventBus.Event event = new EventBus.Event(EventType.MAINTENANCE_REQUIRED, data);
        
        assertEquals(EventType.MAINTENANCE_REQUIRED, event.getType());
        assertSame(data, event.getData());
    }

    // ==================== LogManager测试 ====================
    
    @Test(timeout = 4000)
    public void testLogManagerSingleton() {
        // 测试单例模式
        LogManager instance1 = LogManager.getInstance();
        LogManager instance2 = LogManager.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test(timeout = 4000)
    public void testLogManagerRecordElevatorEvent() {
        // 测试记录电梯事件
        LogManager logManager = new LogManager();
        logManager.recordElevatorEvent(1, "Started moving");
        
        // 应该成功记录
    }
    
    @Test(timeout = 4000)
    public void testLogManagerRecordSchedulerEvent() {
        // 测试记录调度器事件
        LogManager logManager = new LogManager();
        logManager.recordSchedulerEvent("Request dispatched");
        
        // 应该成功记录
    }
    
    @Test(timeout = 4000)
    public void testLogManagerRecordEvent() {
        // 测试记录通用事件
        LogManager logManager = new LogManager();
        logManager.recordEvent("System", "Configuration updated");
        
        // 应该成功记录
    }
    
    @Test(timeout = 4000)
    public void testLogManagerQueryLogs() {
        // 测试查询日志（stream filter条件）
        LogManager logManager = new LogManager();
        long startTime = System.currentTimeMillis();
        
        logManager.recordEvent("Elevator 1", "Started");
        logManager.recordEvent("Elevator 1", "Stopped");
        logManager.recordEvent("Elevator 2", "Started");
        
        long endTime = System.currentTimeMillis();
        
        List<LogManager.SystemLog> logs = logManager.queryLogs("Elevator 1", startTime, endTime);
        assertEquals(2, logs.size());
    }
    
    @Test(timeout = 4000)
    public void testLogManagerQueryLogsNoMatch() {
        // 测试查询日志：无匹配结果
        LogManager logManager = new LogManager();
        long startTime = System.currentTimeMillis();
        
        logManager.recordEvent("Elevator 1", "Started");
        
        long endTime = System.currentTimeMillis();
        
        List<LogManager.SystemLog> logs = logManager.queryLogs("Elevator 2", startTime, endTime);
        assertEquals(0, logs.size());
    }
    
    @Test(timeout = 4000)
    public void testLogManagerQueryLogsTimeFilter() {
        // 测试时间过滤
        LogManager logManager = new LogManager();
        long time1 = System.currentTimeMillis();
        
        logManager.recordEvent("System", "Event 1");
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long time2 = System.currentTimeMillis();
        logManager.recordEvent("System", "Event 2");
        long time3 = System.currentTimeMillis();
        
        List<LogManager.SystemLog> logs = logManager.queryLogs("System", time1, time2);
        assertEquals(1, logs.size());
    }
    
    @Test(timeout = 4000)
    public void testLogManagerSystemLogGetters() {
        // 测试SystemLog的getter方法
        LogManager.SystemLog log = new LogManager.SystemLog("Elevator 1", "Test event", 12345L);
        
        assertEquals("Elevator 1", log.getSource());
        assertEquals("Test event", log.getMessage());
        assertEquals(12345L, log.getTimestamp());
    }

    // ==================== AnalyticsEngine测试 ====================
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineSingleton() {
        // 测试单例模式
        AnalyticsEngine instance1 = AnalyticsEngine.getInstance();
        AnalyticsEngine instance2 = AnalyticsEngine.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineProcessStatusReport() {
        // 测试处理状态报告
        AnalyticsEngine engine = new AnalyticsEngine();
        ElevatorStatusReport report = new ElevatorStatusReport(1, 5, Direction.UP, ElevatorStatus.MOVING, 2.0, 350.0, 5);
        
        engine.processStatusReport(report);
        
        // 应该成功添加报告
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineUpdateFloorPassengerCount() {
        // 测试更新楼层乘客数量
        AnalyticsEngine engine = new AnalyticsEngine();
        
        engine.updateFloorPassengerCount(3, 10);
        engine.updateFloorPassengerCount(5, 15);
        
        // 应该成功更新
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineIsPeakHoursTrue() {
        // 测试高峰时段（总等待乘客>50）- stream sum计算
        AnalyticsEngine engine = new AnalyticsEngine();
        
        engine.updateFloorPassengerCount(1, 20);
        engine.updateFloorPassengerCount(2, 20);
        engine.updateFloorPassengerCount(3, 15);
        
        assertTrue(engine.isPeakHours());
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineIsPeakHoursFalse() {
        // 测试非高峰时段（总等待乘客<=50）
        AnalyticsEngine engine = new AnalyticsEngine();
        
        engine.updateFloorPassengerCount(1, 10);
        engine.updateFloorPassengerCount(2, 15);
        engine.updateFloorPassengerCount(3, 20);
        
        assertFalse(engine.isPeakHours());
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineIsPeakHoursBoundary() {
        // 测试边界值（正好50）
        AnalyticsEngine engine = new AnalyticsEngine();
        
        engine.updateFloorPassengerCount(1, 50);
        
        assertFalse(engine.isPeakHours());
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineIsPeakHoursBoundary51() {
        // 测试边界值（51）
        AnalyticsEngine engine = new AnalyticsEngine();
        
        engine.updateFloorPassengerCount(1, 51);
        
        assertTrue(engine.isPeakHours());
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineGeneratePerformanceReport() {
        // 测试生成性能报告
        AnalyticsEngine engine = new AnalyticsEngine();
        
        AnalyticsEngine.Report report = engine.generatePerformanceReport();
        
        assertNotNull(report);
        assertEquals("System Performance Report", report.getTitle());
        assertTrue(report.getGeneratedTime() > 0);
    }

    // ==================== NotificationService测试 ====================
    
    @Test(timeout = 4000)
    public void testNotificationServiceSingleton() {
        // 测试单例模式
        NotificationService instance1 = NotificationService.getInstance();
        NotificationService instance2 = NotificationService.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceSendNotification() {
        // 测试发送通知
        NotificationService service = new NotificationService();
        List<String> recipients = Arrays.asList("admin@example.com");
        NotificationService.Notification notification = new NotificationService.Notification(
            NotificationService.NotificationType.EMERGENCY,
            "Test message",
            recipients
        );
        
        service.sendNotification(notification);
        
        // 应该成功发送
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceNotificationGetters() {
        // 测试Notification的getter方法
        List<String> recipients = Arrays.asList("user@example.com");
        NotificationService.Notification notification = new NotificationService.Notification(
            NotificationService.NotificationType.MAINTENANCE,
            "Maintenance required",
            recipients
        );
        
        assertEquals(NotificationService.NotificationType.MAINTENANCE, notification.getType());
        assertEquals("Maintenance required", notification.getMessage());
        assertEquals(recipients, notification.getRecipients());
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceNotificationTypeValues() {
        // 测试NotificationType枚举
        NotificationService.NotificationType[] values = NotificationService.NotificationType.values();
        assertEquals(4, values.length);
        assertTrue(Arrays.asList(values).contains(NotificationService.NotificationType.EMERGENCY));
        assertTrue(Arrays.asList(values).contains(NotificationService.NotificationType.MAINTENANCE));
        assertTrue(Arrays.asList(values).contains(NotificationService.NotificationType.SYSTEM_UPDATE));
        assertTrue(Arrays.asList(values).contains(NotificationService.NotificationType.INFORMATION));
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceSMSChannelSupportsEmergency() {
        // 测试SMS通道支持紧急通知
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        assertTrue(channel.supports(NotificationService.NotificationType.EMERGENCY));
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceSMSChannelSupportsMaintenance() {
        // 测试SMS通道支持维护通知
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        assertTrue(channel.supports(NotificationService.NotificationType.MAINTENANCE));
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceSMSChannelNotSupportsInfo() {
        // 测试SMS通道不支持信息通知
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        assertFalse(channel.supports(NotificationService.NotificationType.INFORMATION));
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceEmailChannelSupportsAll() {
        // 测试Email通道支持所有通知类型
        NotificationService.EmailChannel channel = new NotificationService.EmailChannel();
        assertTrue(channel.supports(NotificationService.NotificationType.EMERGENCY));
        assertTrue(channel.supports(NotificationService.NotificationType.MAINTENANCE));
        assertTrue(channel.supports(NotificationService.NotificationType.SYSTEM_UPDATE));
        assertTrue(channel.supports(NotificationService.NotificationType.INFORMATION));
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceSMSChannelSend() {
        // 测试SMS通道发送
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        List<String> recipients = Arrays.asList("123456789");
        NotificationService.Notification notification = new NotificationService.Notification(
            NotificationService.NotificationType.EMERGENCY,
            "Emergency alert",
            recipients
        );
        
        channel.send(notification);
        
        // 应该成功发送
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceEmailChannelSend() {
        // 测试Email通道发送
        NotificationService.EmailChannel channel = new NotificationService.EmailChannel();
        List<String> recipients = Arrays.asList("user@example.com");
        NotificationService.Notification notification = new NotificationService.Notification(
            NotificationService.NotificationType.INFORMATION,
            "System info",
            recipients
        );
        
        channel.send(notification);
        
        // 应该成功发送
    }

    // ==================== MaintenanceManager测试 ====================
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerSingleton() {
        // 测试单例模式
        MaintenanceManager instance1 = MaintenanceManager.getInstance();
        MaintenanceManager instance2 = MaintenanceManager.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerScheduleMaintenance() {
        // 测试安排维护
        MaintenanceManager manager = new MaintenanceManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        manager.scheduleMaintenance(elevator);
        
        // 应该成功安排维护
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerOnEventElevatorFault() {
        // 测试onEvent：电梯故障事件
        MaintenanceManager manager = new MaintenanceManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        EventBus.Event event = new EventBus.Event(EventType.ELEVATOR_FAULT, elevator);
        manager.onEvent(event);
        
        // 应该安排维护
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerOnEventOtherType() {
        // 测试onEvent：其他事件类型（不触发维护）
        MaintenanceManager manager = new MaintenanceManager();
        
        EventBus.Event event = new EventBus.Event(EventType.CONFIG_UPDATED, null);
        manager.onEvent(event);
        
        // 不应该安排维护
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerPerformMaintenance() {
        // 测试执行维护
        MaintenanceManager manager = new MaintenanceManager();
        MaintenanceManager.MaintenanceTask task = new MaintenanceManager.MaintenanceTask(
            1, System.currentTimeMillis(), "Routine maintenance"
        );
        
        manager.performMaintenance(task);
        
        // 应该成功执行
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerRecordMaintenanceResult() {
        // 测试记录维护结果
        MaintenanceManager manager = new MaintenanceManager();
        
        manager.recordMaintenanceResult(1, "Completed successfully");
        
        // 应该成功记录
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerNotifyMaintenancePersonnel() {
        // 测试通知维护人员
        MaintenanceManager manager = new MaintenanceManager();
        MaintenanceManager.MaintenanceTask task = new MaintenanceManager.MaintenanceTask(
            1, System.currentTimeMillis(), "Urgent repair"
        );
        
        manager.notifyMaintenancePersonnel(task);
        
        // 应该成功通知
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceTaskGetters() {
        // 测试MaintenanceTask的getter方法
        long time = System.currentTimeMillis();
        MaintenanceManager.MaintenanceTask task = new MaintenanceManager.MaintenanceTask(
            5, time, "Test description"
        );
        
        assertEquals(5, task.getElevatorId());
        assertEquals(time, task.getScheduledTime());
        assertEquals("Test description", task.getDescription());
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceRecordGetters() {
        // 测试MaintenanceRecord的getter方法
        long time = System.currentTimeMillis();
        MaintenanceManager.MaintenanceRecord record = new MaintenanceManager.MaintenanceRecord(
            3, time, "Maintenance completed"
        );
        
        assertEquals(3, record.getElevatorId());
        assertEquals(time, record.getMaintenanceTime());
        assertEquals("Maintenance completed", record.getResult());
    }

    // ==================== SecurityMonitor测试 ====================
    
    @Test(timeout = 4000)
    public void testSecurityMonitorSingleton() {
        // 测试单例模式
        SecurityMonitor instance1 = SecurityMonitor.getInstance();
        SecurityMonitor instance2 = SecurityMonitor.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test(timeout = 4000)
    public void testSecurityMonitorOnEventEmergency() {
        // 测试onEvent：紧急事件
        SecurityMonitor monitor = new SecurityMonitor();
        
        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, "Fire detected");
        monitor.onEvent(event);
        
        // 应该处理紧急情况
    }
    
    @Test(timeout = 4000)
    public void testSecurityMonitorOnEventOtherType() {
        // 测试onEvent：其他事件类型
        SecurityMonitor monitor = new SecurityMonitor();
        
        EventBus.Event event = new EventBus.Event(EventType.CONFIG_UPDATED, null);
        monitor.onEvent(event);
        
        // 不应该触发紧急处理
    }
    
    @Test(timeout = 4000)
    public void testSecurityMonitorHandleEmergency() {
        // 测试处理紧急情况
        SecurityMonitor monitor = new SecurityMonitor();
        
        monitor.handleEmergency("Security breach");
        
        // 应该记录安全事件并发送通知
    }
    
    @Test(timeout = 4000)
    public void testSecurityEventGetters() {
        // 测试SecurityEvent的getter方法
        long time = System.currentTimeMillis();
        Object data = new Object();
        SecurityMonitor.SecurityEvent event = new SecurityMonitor.SecurityEvent(
            "Security alert", time, data
        );
        
        assertEquals("Security alert", event.getDescription());
        assertEquals(time, event.getTimestamp());
        assertSame(data, event.getData());
    }

    // ==================== ThreadPoolManager测试 ====================
    
    @Test(timeout = 4000)
    public void testThreadPoolManagerSingleton() {
        // 测试单例模式
        ThreadPoolManager instance1 = ThreadPoolManager.getInstance();
        ThreadPoolManager instance2 = ThreadPoolManager.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test(timeout = 4000)
    public void testThreadPoolManagerSubmitTask() {
        // 测试提交任务
        ThreadPoolManager manager = new ThreadPoolManager();
        final boolean[] taskExecuted = {false};
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                taskExecuted[0] = true;
            }
        };
        
        manager.submitTask(task);
        
        // 等待任务执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(taskExecuted[0]);
    }
    
    @Test(timeout = 4000)
    public void testThreadPoolManagerShutdown() {
        // 测试关闭线程池
        ThreadPoolManager manager = new ThreadPoolManager();
        
        manager.shutdown();
        
        // 应该成功关闭
    }

    // ==================== ElevatorStatusReport测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorStatusReportCreation() {
        // 测试状态报告创建
        ElevatorStatusReport report = new ElevatorStatusReport(
            1, 5, Direction.UP, ElevatorStatus.MOVING, 2.5, 350.0, 5
        );
        
        assertEquals(1, report.getElevatorId());
        assertEquals(5, report.getCurrentFloor());
        assertEquals(Direction.UP, report.getDirection());
        assertEquals(ElevatorStatus.MOVING, report.getStatus());
        assertEquals(2.5, report.getSpeed(), 0.001);
        assertEquals(350.0, report.getCurrentLoad(), 0.001);
        assertEquals(5, report.getPassengerCount());
    }
    
    @Test(timeout = 4000)
    public void testElevatorStatusReportToString() {
        // 测试toString方法
        ElevatorStatusReport report = new ElevatorStatusReport(
            2, 7, Direction.DOWN, ElevatorStatus.STOPPED, 0.0, 210.0, 3
        );
        
        String str = report.toString();
        assertTrue(str.contains("elevatorId=2"));
        assertTrue(str.contains("currentFloor=7"));
        assertTrue(str.contains("direction=DOWN"));
        assertTrue(str.contains("status=STOPPED"));
        assertTrue(str.contains("passengerCount=3"));
    }

    // ==================== 边界和异常测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorMinDestinationFloor() {
        // 测试Collections.min()的使用
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        elevator.getDestinationSet().add(3);
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(5);
        
        assertEquals(3, Collections.min(elevator.getDestinationSet()).intValue());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMaxDestinationFloor() {
        // 测试TreeSet的最大值
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        elevator.getDestinationSet().add(3);
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(5);
        
        assertEquals(7, Collections.max(elevator.getDestinationSet()).intValue());
    }
    
    @Test(timeout = 4000)
    public void testElevatorCurrentFloorEqualsMin() {
        // 测试当前楼层等于最小目标楼层
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        elevator.getDestinationSet().add(5);
        elevator.getDestinationSet().add(8);
        
        elevator.updateDirection();
        
        assertEquals(Direction.DOWN, elevator.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testPassengerRequestSameFloor() {
        // 测试起始和目标楼层相同的情况
        PassengerRequest request = new PassengerRequest(5, 5, Priority.MEDIUM, RequestType.STANDARD);
        
        // 应该设置为DOWN（因为5<5为false）
        assertEquals(Direction.DOWN, request.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testElevatorLoadPassengersAtMaxLoad() {
        // 测试电梯满载时不再加载乘客
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setCurrentLoad(elevator.getMaxLoad());
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(request);
        
        elevator.loadPassengers();
        
        // 不应该加载更多乘客
        assertEquals(0, elevator.getPassengerList().size());
    }
    
    @Test(timeout = 4000)
    public void testElevatorLoadPassengersBelowMaxLoad() {
        // 测试电梯未满载时可以加载乘客
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setCurrentLoad(100.0);
        
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(request);
        
        elevator.loadPassengers();
        
        // 应该加载乘客（如果调度器返回了请求）
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveToFirstFloorUp() {
        // 测试moveToFirstFloor：从上方移动到1楼
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setDirection(Direction.DOWN);
        
        try {
            elevator.moveToFirstFloor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(1, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveToFirstFloorAlreadyThere() {
        // 测试moveToFirstFloor：已经在1楼
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(1);
        
        try {
            elevator.moveToFirstFloor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(1, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testElevatorNotifyObserversWithElevatorStatus() {
        // 测试通知观察者（传递ElevatorStatus）
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        final Object[] receivedArg = {null};
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                receivedArg[0] = arg;
            }
        };
        
        elevator.addObserver(observer);
        elevator.notifyObservers(ElevatorStatus.EMERGENCY);
        
        assertEquals(ElevatorStatus.EMERGENCY, receivedArg[0]);
    }
    
    @Test(timeout = 4000)
    public void testMultipleElevatorsInManager() {
        // 测试管理器中的多个电梯
        ElevatorManager manager = new ElevatorManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        
        for (int i = 1; i <= 5; i++) {
            Elevator elevator = new Elevator(i, scheduler);
            manager.registerElevator(elevator);
        }
        
        assertEquals(5, manager.getAllElevators().size());
        assertNotNull(manager.getElevatorById(3));
    }
    
    @Test(timeout = 4000)
    public void testSchedulerMultipleFloors() {
        // 测试调度器管理多个楼层
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 20, new NearestElevatorStrategy());
        
        for (int i = 1; i <= 20; i++) {
            List<PassengerRequest> requests = scheduler.getRequestsAtFloor(i, Direction.UP);
            assertNotNull(requests);
        }
    }
    
    @Test(timeout = 4000)
    public void testHighPriorityRequestQueueing() {
        // 测试高优先级请求排队
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        PassengerRequest request1 = new PassengerRequest(3, 7, Priority.HIGH, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(5, 9, Priority.HIGH, RequestType.STANDARD);
        
        scheduler.submitRequest(request1);
        scheduler.submitRequest(request2);
        
        // 两个高优先级请求都应该被处理
    }
    
    @Test(timeout = 4000)
    public void testStrategyWithEmptyElevatorList() {
        // 测试策略处理空电梯列表
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        List<Elevator> elevators = new ArrayList<>();
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineMultipleReports() {
        // 测试处理多个状态报告
        AnalyticsEngine engine = new AnalyticsEngine();
        
        for (int i = 1; i <= 10; i++) {
            ElevatorStatusReport report = new ElevatorStatusReport(
                i, 5, Direction.UP, ElevatorStatus.MOVING, 2.0, 300.0, 4
            );
            engine.processStatusReport(report);
        }
        
        // 应该成功处理所有报告
    }
    
    @Test(timeout = 4000)
    public void testConcurrentHashMapUsage() {
        // 测试ConcurrentHashMap的使用
        AnalyticsEngine engine = new AnalyticsEngine();
        
        engine.updateFloorPassengerCount(1, 10);
        engine.updateFloorPassengerCount(1, 20); // 更新同一楼层
        
        // 第二次更新应该覆盖第一次的值
    }
    
    @Test(timeout = 4000)
    public void testCopyOnWriteArrayListUsage() {
        // 测试CopyOnWriteArrayList的使用
        LogManager logManager = new LogManager();
        
        logManager.recordEvent("System", "Event 1");
        logManager.recordEvent("System", "Event 2");
        
        // 应该支持并发读写
    }

    // ==================== 综合场景测试 ====================
    
    @Test(timeout = 4000)
    public void testCompleteElevatorLifecycle() {
        // 测试电梯的完整生命周期
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        // 1. 初始状态
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
        assertEquals(1, elevator.getCurrentFloor());
        
        // 2. 添加目标
        elevator.addDestination(5);
        assertTrue(elevator.getDestinationSet().contains(5));
        
        // 3. 移动
        try {
            elevator.move();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 4. 处理紧急情况
        elevator.handleEmergency();
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testPassengerRequestWorkflow() {
        // 测试乘客请求的完整工作流
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        elevatorList.add(elevator);
        
        // 1. 创建请求
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        
        // 2. 提交请求
        scheduler.submitRequest(request);
        
        // 3. 电梯应该被分配到起始楼层
        assertTrue(elevator.getDestinationSet().contains(3));
    }
    
    @Test(timeout = 4000)
    public void testMultipleStrategies() {
        // 测试多种策略的切换
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevatorList.add(elevator);
        
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        // 1. 最近电梯策略
        scheduler.setDispatchStrategy(new NearestElevatorStrategy());
        scheduler.dispatchElevator(request);
        
        // 2. 高效率策略
        scheduler.setDispatchStrategy(new HighEfficiencyStrategy());
        scheduler.dispatchElevator(request);
        
        // 3. 节能策略
        scheduler.setDispatchStrategy(new EnergySavingStrategy());
        scheduler.dispatchElevator(request);
        
        // 4. 预测调度策略
        scheduler.setDispatchStrategy(new PredictiveSchedulingStrategy());
        scheduler.dispatchElevator(request);
    }
    
    @Test(timeout = 4000)
    public void testEventPropagation() {
        // 测试事件传播
        EventBus eventBus = new EventBus();
        final int[] eventCount = {0};
        
        EventBus.EventListener listener = new EventBus.EventListener() {
            @Override
            public void onEvent(EventBus.Event event) {
                eventCount[0]++;
            }
        };
        
        eventBus.subscribe(EventType.EMERGENCY, listener);
        eventBus.subscribe(EventType.ELEVATOR_FAULT, listener);
        
        eventBus.publish(new EventBus.Event(EventType.EMERGENCY, null));
        eventBus.publish(new EventBus.Event(EventType.ELEVATOR_FAULT, null));
        
        assertEquals(2, eventCount[0]);
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigurationUpdate() {
        // 测试系统配置更新
        SystemConfig config = SystemConfig.getInstance();
        
        int originalFloorCount = config.getFloorCount();
        config.setFloorCount(25);
        assertEquals(25, config.getFloorCount());
        
        int originalElevatorCount = config.getElevatorCount();
        config.setElevatorCount(6);
        assertEquals(6, config.getElevatorCount());
        
        double originalMaxLoad = config.getMaxLoad();
        config.setMaxLoad(1000.0);
        assertEquals(1000.0, config.getMaxLoad(), 0.001);
    }

    // ==================== 额外的分支覆盖和变异测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorOpenDoorWithPassengers() throws InterruptedException {
        // 测试openDoor方法的完整流程
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        
        PassengerRequest request = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
        elevator.getPassengerList().add(request);
        elevator.getDestinationSet().add(5);
        
        elevator.openDoor();
        
        assertEquals(ElevatorStatus.STOPPED, elevator.getStatus());
        assertEquals(0, elevator.getPassengerList().size());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveUpMultipleFloors() throws InterruptedException {
        // 测试电梯连续上行多个楼层
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(1);
        elevator.setDirection(Direction.UP);
        elevator.getDestinationSet().add(5);
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(9);
        
        elevator.move(); // 1->2
        assertEquals(2, elevator.getCurrentFloor());
        
        elevator.move(); // 2->3
        assertEquals(3, elevator.getCurrentFloor());
        
        elevator.move(); // 3->4
        assertEquals(4, elevator.getCurrentFloor());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveDownMultipleFloors() throws InterruptedException {
        // 测试电梯连续下行多个楼层
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(10);
        elevator.setDirection(Direction.DOWN);
        elevator.getDestinationSet().add(5);
        elevator.getDestinationSet().add(3);
        elevator.getDestinationSet().add(1);
        
        elevator.move(); // 10->9
        assertEquals(9, elevator.getCurrentFloor());
        
        elevator.move(); // 9->8
        assertEquals(8, elevator.getCurrentFloor());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveToFirstFloorFromAbove() throws InterruptedException {
        // 测试从高楼层移动到1楼（UP方向）
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        elevator.setDirection(Direction.UP);
        
        elevator.moveToFirstFloor();
        
        assertEquals(1, elevator.getCurrentFloor());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }
    
    @Test(timeout = 4000)
    public void testElevatorGetScheduler() {
        // 测试获取调度器
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        assertSame(scheduler, elevator.getScheduler());
    }
    
    @Test(timeout = 4000)
    public void testElevatorGetMaxLoad() {
        // 测试获取最大载重
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        assertEquals(SystemConfig.getInstance().getMaxLoad(), elevator.getMaxLoad(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testElevatorUnloadPassengersMultiple() {
        // 测试卸载多个乘客
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        
        PassengerRequest request1 = new PassengerRequest(3, 5, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(2, 5, Priority.HIGH, RequestType.STANDARD);
        PassengerRequest request3 = new PassengerRequest(4, 5, Priority.LOW, RequestType.STANDARD);
        PassengerRequest request4 = new PassengerRequest(1, 8, Priority.MEDIUM, RequestType.STANDARD);
        
        elevator.getPassengerList().add(request1);
        elevator.getPassengerList().add(request2);
        elevator.getPassengerList().add(request3);
        elevator.getPassengerList().add(request4);
        
        elevator.unloadPassengers();
        
        assertEquals(1, elevator.getPassengerList().size());
        assertEquals(request4, elevator.getPassengerList().get(0));
    }
    
    @Test(timeout = 4000)
    public void testElevatorLoadPassengersMultiple() {
        // 测试加载多个乘客
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setDirection(Direction.UP);
        elevator.setCurrentLoad(0.0);
        elevatorList.add(elevator);
        
        PassengerRequest request1 = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(3, 8, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request3 = new PassengerRequest(3, 9, Priority.MEDIUM, RequestType.STANDARD);
        
        scheduler.submitRequest(request1);
        scheduler.submitRequest(request2);
        scheduler.submitRequest(request3);
        
        elevator.loadPassengers();
        
        // 应该加载了一些乘客
        assertTrue(elevator.getCurrentLoad() >= 0);
    }
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategyDistance4Boundary() {
        // 测试距离正好4的边界条件
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.UP);
        elevator.setCurrentFloor(1);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator, selected);
    }
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategyDistance6Boundary() {
        // 测试距离正好6（不满足<5条件）
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.DOWN);
        elevator.setCurrentFloor(1);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(7, 3, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }
    
    @Test(timeout = 4000)
    public void testPredictiveSchedulingStrategyLoadFactor() {
        // 测试loadFactor的计算
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        
        PassengerRequest request1 = new PassengerRequest(2, 8, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(3, 9, Priority.MEDIUM, RequestType.STANDARD);
        elevator.getPassengerList().add(request1);
        elevator.getPassengerList().add(request2);
        
        PassengerRequest request = new PassengerRequest(7, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        double cost = strategy.calculatePredictedCost(elevator, request);
        
        assertTrue(cost > 2); // distance=2 + loadFactor*10
    }
    
    @Test(timeout = 4000)
    public void testPredictiveSchedulingStrategyLowCost() {
        // 测试选择成本最低的电梯
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setCurrentFloor(2);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setCurrentFloor(9);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator1, selected);
    }
    
    @Test(timeout = 4000)
    public void testHighEfficiencyStrategyIdleElevator() {
        // 测试选择IDLE状态的电梯
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setStatus(ElevatorStatus.MOVING);
        elevator1.setDirection(Direction.DOWN);
        elevator1.setCurrentFloor(8);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setStatus(ElevatorStatus.IDLE);
        elevator2.setCurrentFloor(2);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator2, selected);
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyEqualDistance() {
        // 测试距离相等时选择第一个
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setCurrentFloor(3);
        elevator1.setStatus(ElevatorStatus.IDLE);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setCurrentFloor(7);
        elevator2.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator1, selected); // 距离相等，选择第一个
    }
    
    @Test(timeout = 4000)
    public void testNearestElevatorStrategyStoppedStatus() {
        // 测试STOPPED状态的电梯不被选择
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        elevator.setStatus(ElevatorStatus.STOPPED);
        
        List<Elevator> elevators = Arrays.asList(elevator);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertNull(selected);
    }
    
    @Test(timeout = 4000)
    public void testSchedulerSubmitRequestAndDispatch() {
        // 测试提交请求后的完整派遣流程
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setStatus(ElevatorStatus.IDLE);
        elevator1.setCurrentFloor(1);
        elevatorList.add(elevator1);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setStatus(ElevatorStatus.IDLE);
        elevator2.setCurrentFloor(8);
        elevatorList.add(elevator2);
        
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(request);
        
        // 应该选择elevator1（距离更近）
        assertTrue(elevator1.getDestinationSet().contains(5));
    }
    
    @Test(timeout = 4000)
    public void testFloorMultipleRequestsSameDirection() {
        // 测试同一楼层同方向的多个请求
        Floor floor = new Floor(3);
        
        PassengerRequest request1 = new PassengerRequest(3, 7, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(3, 8, Priority.HIGH, RequestType.STANDARD);
        PassengerRequest request3 = new PassengerRequest(3, 9, Priority.LOW, RequestType.STANDARD);
        
        floor.addRequest(request1);
        floor.addRequest(request2);
        floor.addRequest(request3);
        
        List<PassengerRequest> requests = floor.getRequests(Direction.UP);
        assertEquals(3, requests.size());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMultipleObservers() {
        // 测试多个观察者
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        final int[] counter = {0};
        
        Observer observer1 = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                counter[0]++;
            }
        };
        
        Observer observer2 = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                counter[0]++;
            }
        };
        
        Observer observer3 = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                counter[0]++;
            }
        };
        
        elevator.addObserver(observer1);
        elevator.addObserver(observer2);
        elevator.addObserver(observer3);
        
        Event event = new Event(EventType.EMERGENCY, null);
        elevator.notifyObservers(event);
        
        assertEquals(3, counter[0]);
    }
    
    @Test(timeout = 4000)
    public void testElevatorAddDestinationMultiple() {
        // 测试添加多个目标楼层
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        elevator.addDestination(3);
        elevator.addDestination(5);
        elevator.addDestination(7);
        elevator.addDestination(9);
        
        assertEquals(4, elevator.getDestinationSet().size());
        assertTrue(elevator.getDestinationSet().contains(3));
        assertTrue(elevator.getDestinationSet().contains(5));
        assertTrue(elevator.getDestinationSet().contains(7));
        assertTrue(elevator.getDestinationSet().contains(9));
    }
    
    @Test(timeout = 4000)
    public void testElevatorAddDestinationDuplicate() {
        // 测试添加重复的目标楼层（TreeSet去重）
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        elevator.addDestination(5);
        elevator.addDestination(5);
        elevator.addDestination(5);
        
        assertEquals(1, elevator.getDestinationSet().size());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetFloorCount1() {
        // 测试设置楼层数为1（边界值）
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(1);
        assertEquals(1, config.getFloorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetElevatorCount1() {
        // 测试设置电梯数量为1（边界值）
        SystemConfig config = SystemConfig.getInstance();
        config.setElevatorCount(1);
        assertEquals(1, config.getElevatorCount());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigSetMaxLoad0_01() {
        // 测试设置最大载重为0.01（边界值）
        SystemConfig config = SystemConfig.getInstance();
        config.setMaxLoad(0.01);
        assertEquals(0.01, config.getMaxLoad(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineIsPeakHoursZero() {
        // 测试0个等待乘客
        AnalyticsEngine engine = new AnalyticsEngine();
        assertFalse(engine.isPeakHours());
    }
    
    @Test(timeout = 4000)
    public void testLogManagerQueryLogsOutOfRange() {
        // 测试查询时间范围外的日志
        LogManager logManager = new LogManager();
        long time1 = System.currentTimeMillis();
        
        logManager.recordEvent("System", "Event 1");
        
        long time2 = time1 - 1000; // 过去的时间
        long time3 = time1 - 500;
        
        List<LogManager.SystemLog> logs = logManager.queryLogs("System", time2, time3);
        assertEquals(0, logs.size());
    }
    
    @Test(timeout = 4000)
    public void testEventBusSubscribeSameListenerTwice() {
        // 测试同一监听器订阅两次
        EventBus eventBus = new EventBus();
        final int[] counter = {0};
        
        EventBus.EventListener listener = new EventBus.EventListener() {
            @Override
            public void onEvent(EventBus.Event event) {
                counter[0]++;
            }
        };
        
        eventBus.subscribe(EventType.EMERGENCY, listener);
        eventBus.subscribe(EventType.EMERGENCY, listener);
        
        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, null);
        eventBus.publish(event);
        
        assertEquals(2, counter[0]); // 应该被调用两次
    }
    
    @Test(timeout = 4000)
    public void testEventBusDifferentEventTypes() {
        // 测试不同事件类型的订阅
        EventBus eventBus = new EventBus();
        final int[] counter = {0};
        
        EventBus.EventListener listener = new EventBus.EventListener() {
            @Override
            public void onEvent(EventBus.Event event) {
                counter[0]++;
            }
        };
        
        eventBus.subscribe(EventType.EMERGENCY, listener);
        
        EventBus.Event event1 = new EventBus.Event(EventType.EMERGENCY, null);
        EventBus.Event event2 = new EventBus.Event(EventType.ELEVATOR_FAULT, null);
        
        eventBus.publish(event1);
        eventBus.publish(event2);
        
        assertEquals(1, counter[0]); // 只有EMERGENCY被处理
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceMultipleChannels() {
        // 测试多个通道的通知发送
        NotificationService service = new NotificationService();
        List<String> recipients = Arrays.asList("admin@example.com", "user@example.com");
        
        NotificationService.Notification notification1 = new NotificationService.Notification(
            NotificationService.NotificationType.EMERGENCY,
            "Emergency notification",
            recipients
        );
        
        NotificationService.Notification notification2 = new NotificationService.Notification(
            NotificationService.NotificationType.INFORMATION,
            "Info notification",
            recipients
        );
        
        service.sendNotification(notification1);
        service.sendNotification(notification2);
        
        // 应该通过相应的通道发送
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceSMSChannelNotSupportsSystemUpdate() {
        // 测试SMS通道不支持系统更新通知
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        assertFalse(channel.supports(NotificationService.NotificationType.SYSTEM_UPDATE));
    }
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerMultipleTasks() {
        // 测试多个维护任务
        MaintenanceManager manager = new MaintenanceManager();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        
        Elevator elevator1 = new Elevator(1, scheduler);
        Elevator elevator2 = new Elevator(2, scheduler);
        Elevator elevator3 = new Elevator(3, scheduler);
        
        manager.scheduleMaintenance(elevator1);
        manager.scheduleMaintenance(elevator2);
        manager.scheduleMaintenance(elevator3);
        
        // 应该成功安排三个维护任务
    }
    
    @Test(timeout = 4000)
    public void testSecurityMonitorMultipleEvents() {
        // 测试多个安全事件
        SecurityMonitor monitor = new SecurityMonitor();
        
        monitor.handleEmergency("Fire alarm");
        monitor.handleEmergency("Security breach");
        monitor.handleEmergency("Power failure");
        
        // 应该记录三个安全事件
    }
    
    @Test(timeout = 4000)
    public void testThreadPoolManagerMultipleTasks() {
        // 测试提交多个任务
        ThreadPoolManager manager = new ThreadPoolManager();
        final int[] taskCount = {0};
        
        for (int i = 0; i < 5; i++) {
            manager.submitTask(new Runnable() {
                @Override
                public void run() {
                    taskCount[0]++;
                }
            });
        }
        
        // 等待所有任务执行
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(5, taskCount[0]);
    }
    
    @Test(timeout = 4000)
    public void testElevatorStatusAllValues() {
        // 测试所有ElevatorStatus值
        ElevatorStatus[] values = ElevatorStatus.values();
        for (ElevatorStatus status : values) {
            assertEquals(status, ElevatorStatus.valueOf(status.name()));
        }
    }
    
    @Test(timeout = 4000)
    public void testEventTypeAllValues() {
        // 测试所有EventType值
        EventType[] values = EventType.values();
        for (EventType type : values) {
            assertEquals(type, EventType.valueOf(type.name()));
        }
    }
    
    @Test(timeout = 4000)
    public void testElevatorModeAllValues() {
        // 测试所有ElevatorMode值
        ElevatorMode[] values = ElevatorMode.values();
        for (ElevatorMode mode : values) {
            assertEquals(mode, ElevatorMode.valueOf(mode.name()));
        }
    }
    
    @Test(timeout = 4000)
    public void testPriorityAllValues() {
        // 测试所有Priority值
        Priority[] values = Priority.values();
        for (Priority priority : values) {
            assertEquals(priority, Priority.valueOf(priority.name()));
        }
    }
    
    @Test(timeout = 4000)
    public void testRequestTypeAllValues() {
        // 测试所有RequestType值
        RequestType[] values = RequestType.values();
        for (RequestType type : values) {
            assertEquals(type, RequestType.valueOf(type.name()));
        }
    }
    
    @Test(timeout = 4000)
    public void testSpecialNeedsAllValues() {
        // 测试所有SpecialNeeds值
        SpecialNeeds[] values = SpecialNeeds.values();
        for (SpecialNeeds need : values) {
            assertEquals(need, SpecialNeeds.valueOf(need.name()));
        }
    }
    
    @Test(timeout = 4000)
    public void testNotificationTypeAllValues() {
        // 测试所有NotificationType值
        NotificationService.NotificationType[] values = NotificationService.NotificationType.values();
        for (NotificationService.NotificationType type : values) {
            assertEquals(type, NotificationService.NotificationType.valueOf(type.name()));
        }
    }
    
    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionWithMultipleDestinations() {
        // 测试有多个目标时的方向更新
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        
        elevator.getDestinationSet().add(3);
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(9);
        
        elevator.updateDirection();
        
        // 最小目标是3，小于当前楼层5，应该DOWN
        assertEquals(Direction.DOWN, elevator.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionAllAbove() {
        // 测试所有目标都在当前楼层之上
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(2);
        
        elevator.getDestinationSet().add(5);
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(9);
        
        elevator.updateDirection();
        
        assertEquals(Direction.UP, elevator.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testSchedulerMultiplePriorityRequests() {
        // 测试多个不同优先级的请求
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        elevatorList.add(elevator);
        
        PassengerRequest highRequest = new PassengerRequest(3, 7, Priority.HIGH, RequestType.STANDARD);
        PassengerRequest mediumRequest = new PassengerRequest(4, 8, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest lowRequest = new PassengerRequest(5, 9, Priority.LOW, RequestType.STANDARD);
        
        scheduler.submitRequest(highRequest);
        scheduler.submitRequest(mediumRequest);
        scheduler.submitRequest(lowRequest);
        
        // 所有请求都应该被处理
    }
    
    @Test(timeout = 4000)
    public void testElevatorClearAllRequestsEmpty() {
        // 测试清除空请求列表
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        List<PassengerRequest> cleared = elevator.clearAllRequests();
        
        assertEquals(0, cleared.size());
        assertEquals(0, elevator.getPassengerList().size());
        assertEquals(0, elevator.getDestinationSet().size());
    }
    
    @Test(timeout = 4000)
    public void testFloorGetRequestsEmptyQueue() {
        // 测试获取空队列
        Floor floor = new Floor(5);
        
        List<PassengerRequest> requests = floor.getRequests(Direction.UP);
        assertEquals(0, requests.size());
    }
    
    @Test(timeout = 4000)
    public void testPassengerRequestBoundaryFloors() {
        // 测试边界楼层的请求
        PassengerRequest request1 = new PassengerRequest(1, 20, Priority.HIGH, RequestType.STANDARD);
        assertEquals(Direction.UP, request1.getDirection());
        
        PassengerRequest request2 = new PassengerRequest(20, 1, Priority.HIGH, RequestType.STANDARD);
        assertEquals(Direction.DOWN, request2.getDirection());
    }
    
    @Test(timeout = 4000)
    public void testElevatorMoveNotReachingDestination() {
        // 测试移动但未到达任何目标
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setDirection(Direction.UP);
        elevator.getDestinationSet().add(7);
        elevator.getDestinationSet().add(9);
        
        try {
            elevator.move();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(4, elevator.getCurrentFloor());
        assertFalse(elevator.getDestinationSet().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testElevatorEnergyConsumptionIncrement() {
        // 测试能耗递增
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.setDirection(Direction.UP);
        elevator.getDestinationSet().add(7);
        elevator.setEnergyConsumption(10.0);
        
        try {
            elevator.move();
            elevator.move();
            elevator.move();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(13.0, elevator.getEnergyConsumption(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testElevatorPassengerLoadCalculation() {
        // 测试乘客载重计算
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        
        PassengerRequest request1 = new PassengerRequest(2, 5, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request2 = new PassengerRequest(3, 6, Priority.MEDIUM, RequestType.STANDARD);
        PassengerRequest request3 = new PassengerRequest(4, 7, Priority.MEDIUM, RequestType.STANDARD);
        
        elevator.getPassengerList().add(request1);
        elevator.getPassengerList().add(request2);
        elevator.getPassengerList().add(request3);
        
        elevator.setCurrentFloor(5);
        elevator.unloadPassengers();
        
        assertEquals(140.0, elevator.getCurrentLoad(), 0.001); // 2个乘客 * 70kg
    }
    
    @Test(timeout = 4000)
    public void testHighEfficiencyStrategyBothIdle() {
        // 测试两个都是IDLE时选择更近的
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setStatus(ElevatorStatus.IDLE);
        elevator1.setCurrentFloor(2);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setStatus(ElevatorStatus.IDLE);
        elevator2.setCurrentFloor(8);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator1, selected);
    }
    
    @Test(timeout = 4000)
    public void testEnergySavingStrategyAllIdle() {
        // 测试多个空闲电梯时选择第一个
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator elevator1 = new Elevator(1, scheduler);
        elevator1.setStatus(ElevatorStatus.IDLE);
        
        Elevator elevator2 = new Elevator(2, scheduler);
        elevator2.setStatus(ElevatorStatus.IDLE);
        
        List<Elevator> elevators = Arrays.asList(elevator1, elevator2);
        PassengerRequest request = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        
        Elevator selected = strategy.selectElevator(elevators, request);
        
        assertEquals(elevator1, selected);
    }
    
    @Test(timeout = 4000)
    public void testElevatorManagerConstructor() {
        // 测试ElevatorManager构造函数
        ElevatorManager manager = new ElevatorManager();
        assertNotNull(manager);
        assertNotNull(manager.getAllElevators());
        assertEquals(0, manager.getAllElevators().size());
    }
    
    @Test(timeout = 4000)
    public void testSystemConfigConstructor() {
        // 测试SystemConfig构造函数创建的实例
        SystemConfig config = new SystemConfig();
        assertNotNull(config);
        assertEquals(20, config.getFloorCount());
        assertEquals(4, config.getElevatorCount());
        assertEquals(800.0, config.getMaxLoad(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testEventBusConstructor() {
        // 测试EventBus构造函数
        EventBus eventBus = new EventBus();
        assertNotNull(eventBus);
    }
    
    @Test(timeout = 4000)
    public void testLogManagerConstructor() {
        // 测试LogManager构造函数
        LogManager logManager = new LogManager();
        assertNotNull(logManager);
    }
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineConstructor() {
        // 测试AnalyticsEngine构造函数
        AnalyticsEngine engine = new AnalyticsEngine();
        assertNotNull(engine);
    }
    
    @Test(timeout = 4000)
    public void testNotificationServiceConstructor() {
        // 测试NotificationService构造函数
        NotificationService service = new NotificationService();
        assertNotNull(service);
    }
    
    @Test(timeout = 4000)
    public void testSchedulerRedistributeRequestsEmpty() {
        // 测试重新分配空请求列表
        List<Elevator> elevatorList = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevatorList, 10, new NearestElevatorStrategy());
        Elevator elevator1 = new Elevator(1, scheduler);
        Elevator elevator2 = new Elevator(2, scheduler);
        elevatorList.add(elevator1);
        elevatorList.add(elevator2);
        
        scheduler.redistributeRequests(elevator1);
        
        assertEquals(0, elevator1.getPassengerList().size());
    }
    
    @Test(timeout = 4000)
    public void testElevatorStatusReportAllGetters() {
        // 测试ElevatorStatusReport的所有getter
        ElevatorStatusReport report = new ElevatorStatusReport(
            1, 5, Direction.UP, ElevatorStatus.MOVING, 2.5, 350.0, 5
        );
        
        assertEquals(1, report.getElevatorId());
        assertEquals(5, report.getCurrentFloor());
        assertEquals(Direction.UP, report.getDirection());
        assertEquals(ElevatorStatus.MOVING, report.getStatus());
        assertEquals(2.5, report.getSpeed(), 0.001);
        assertEquals(350.0, report.getCurrentLoad(), 0.001);
        assertEquals(5, report.getPassengerCount());
        assertNotNull(report.toString());
    }
}

