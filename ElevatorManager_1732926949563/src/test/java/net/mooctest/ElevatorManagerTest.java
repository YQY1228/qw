package net.mooctest;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;

public class ElevatorManagerTest {

    // ==================== 枚举类完整覆盖 ====================
    
    @Test(timeout = 4000)
    public void testDirectionEnum() {
        Direction[] values = Direction.values();
        assertEquals(2, values.length);
        assertEquals(Direction.UP, Direction.valueOf("UP"));
        assertEquals(Direction.DOWN, Direction.valueOf("DOWN"));
        assertNotNull(Direction.UP.toString());
        assertNotNull(Direction.DOWN.toString());
    }

    @Test(timeout = 4000)
    public void testElevatorStatusEnum() {
        ElevatorStatus[] values = ElevatorStatus.values();
        assertEquals(6, values.length);
        assertEquals(ElevatorStatus.MOVING, ElevatorStatus.valueOf("MOVING"));
        assertEquals(ElevatorStatus.STOPPED, ElevatorStatus.valueOf("STOPPED"));
        assertEquals(ElevatorStatus.IDLE, ElevatorStatus.valueOf("IDLE"));
        assertEquals(ElevatorStatus.MAINTENANCE, ElevatorStatus.valueOf("MAINTENANCE"));
        assertEquals(ElevatorStatus.EMERGENCY, ElevatorStatus.valueOf("EMERGENCY"));
        assertEquals(ElevatorStatus.FAULT, ElevatorStatus.valueOf("FAULT"));
    }

    @Test(timeout = 4000)
    public void testElevatorModeEnum() {
        ElevatorMode[] values = ElevatorMode.values();
        assertEquals(3, values.length);
        assertEquals(ElevatorMode.NORMAL, ElevatorMode.valueOf("NORMAL"));
        assertEquals(ElevatorMode.ENERGY_SAVING, ElevatorMode.valueOf("ENERGY_SAVING"));
        assertEquals(ElevatorMode.EMERGENCY, ElevatorMode.valueOf("EMERGENCY"));
    }

    @Test(timeout = 4000)
    public void testPriorityEnum() {
        Priority[] values = Priority.values();
        assertEquals(3, values.length);
        assertEquals(Priority.HIGH, Priority.valueOf("HIGH"));
        assertEquals(Priority.MEDIUM, Priority.valueOf("MEDIUM"));
        assertEquals(Priority.LOW, Priority.valueOf("LOW"));
    }

    @Test(timeout = 4000)
    public void testRequestTypeEnum() {
        RequestType[] values = RequestType.values();
        assertEquals(2, values.length);
        assertEquals(RequestType.STANDARD, RequestType.valueOf("STANDARD"));
        assertEquals(RequestType.DESTINATION_CONTROL, RequestType.valueOf("DESTINATION_CONTROL"));
    }

    @Test(timeout = 4000)
    public void testSpecialNeedsEnum() {
        SpecialNeeds[] values = SpecialNeeds.values();
        assertEquals(4, values.length);
        assertEquals(SpecialNeeds.NONE, SpecialNeeds.valueOf("NONE"));
        assertEquals(SpecialNeeds.DISABLED_ASSISTANCE, SpecialNeeds.valueOf("DISABLED_ASSISTANCE"));
        assertEquals(SpecialNeeds.LARGE_LUGGAGE, SpecialNeeds.valueOf("LARGE_LUGGAGE"));
        assertEquals(SpecialNeeds.VIP_SERVICE, SpecialNeeds.valueOf("VIP_SERVICE"));
    }

    @Test(timeout = 4000)
    public void testEventTypeEnum() {
        EventType[] values = EventType.values();
        assertEquals(4, values.length);
        assertEquals(EventType.ELEVATOR_FAULT, EventType.valueOf("ELEVATOR_FAULT"));
        assertEquals(EventType.EMERGENCY, EventType.valueOf("EMERGENCY"));
        assertEquals(EventType.MAINTENANCE_REQUIRED, EventType.valueOf("MAINTENANCE_REQUIRED"));
        assertEquals(EventType.CONFIG_UPDATED, EventType.valueOf("CONFIG_UPDATED"));
    }

    @Test(timeout = 4000)
    public void testNotificationTypeEnum() {
        NotificationService.NotificationType[] values = NotificationService.NotificationType.values();
        assertEquals(4, values.length);
        assertEquals(NotificationService.NotificationType.EMERGENCY, 
            NotificationService.NotificationType.valueOf("EMERGENCY"));
        assertEquals(NotificationService.NotificationType.MAINTENANCE,
            NotificationService.NotificationType.valueOf("MAINTENANCE"));
        assertEquals(NotificationService.NotificationType.SYSTEM_UPDATE,
            NotificationService.NotificationType.valueOf("SYSTEM_UPDATE"));
        assertEquals(NotificationService.NotificationType.INFORMATION,
            NotificationService.NotificationType.valueOf("INFORMATION"));
    }

    // ==================== SystemConfig 完整测试 ====================
    
    @Test(timeout = 4000)
    public void testSystemConfigSingleton() {
        SystemConfig c1 = SystemConfig.getInstance();
        SystemConfig c2 = SystemConfig.getInstance();
        assertNotNull(c1);
        assertSame(c1, c2);
    }

    @Test(timeout = 4000)
    public void testSystemConfigDefaults() {
        SystemConfig config = new SystemConfig();
        assertEquals(20, config.getFloorCount());
        assertEquals(4, config.getElevatorCount());
        assertEquals(800.0, config.getMaxLoad(), 0.001);
    }

    @Test(timeout = 4000)
    public void testSystemConfigSetFloorCountPositive() {
        SystemConfig config = SystemConfig.getInstance();
        config.setFloorCount(25);
        assertEquals(25, config.getFloorCount());
    }

    @Test(timeout = 4000)
    public void testSystemConfigSetFloorCountZero() {
        SystemConfig config = SystemConfig.getInstance();
        int before = config.getFloorCount();
        config.setFloorCount(0);
        assertEquals(before, config.getFloorCount());
    }

    @Test(timeout = 4000)
    public void testSystemConfigSetFloorCountNegative() {
        SystemConfig config = SystemConfig.getInstance();
        int before = config.getFloorCount();
        config.setFloorCount(-5);
        assertEquals(before, config.getFloorCount());
    }

    @Test(timeout = 4000)
    public void testSystemConfigSetElevatorCountPositive() {
        SystemConfig config = SystemConfig.getInstance();
        config.setElevatorCount(8);
        assertEquals(8, config.getElevatorCount());
    }

    @Test(timeout = 4000)
    public void testSystemConfigSetElevatorCountZero() {
        SystemConfig config = SystemConfig.getInstance();
        int before = config.getElevatorCount();
        config.setElevatorCount(0);
        assertEquals(before, config.getElevatorCount());
    }

    @Test(timeout = 4000)
    public void testSystemConfigSetMaxLoadPositive() {
        SystemConfig config = SystemConfig.getInstance();
        config.setMaxLoad(1000.0);
        assertEquals(1000.0, config.getMaxLoad(), 0.001);
    }

    @Test(timeout = 4000)
    public void testSystemConfigSetMaxLoadZero() {
        SystemConfig config = SystemConfig.getInstance();
        double before = config.getMaxLoad();
        config.setMaxLoad(0.0);
        assertEquals(before, config.getMaxLoad(), 0.001);
    }

    // ==================== PassengerRequest 测试 ====================
    
    @Test(timeout = 4000)
    public void testPassengerRequestUpDirection() {
        PassengerRequest req = new PassengerRequest(1, 10, Priority.HIGH, RequestType.STANDARD);
        assertEquals(1, req.getStartFloor());
        assertEquals(10, req.getDestinationFloor());
        assertEquals(Direction.UP, req.getDirection());
        assertEquals(Priority.HIGH, req.getPriority());
        assertEquals(RequestType.STANDARD, req.getRequestType());
        assertEquals(SpecialNeeds.NONE, req.getSpecialNeeds());
        assertTrue(req.getTimestamp() > 0);
    }

    @Test(timeout = 4000)
    public void testPassengerRequestDownDirection() {
        PassengerRequest req = new PassengerRequest(10, 1, Priority.LOW, RequestType.DESTINATION_CONTROL);
        assertEquals(Direction.DOWN, req.getDirection());
    }

    @Test(timeout = 4000)
    public void testPassengerRequestToString() {
        PassengerRequest req = new PassengerRequest(5, 15, Priority.MEDIUM, RequestType.STANDARD);
        String str = req.toString();
        assertNotNull(str);
        assertTrue(str.contains("5"));
        assertTrue(str.contains("15"));
    }

    // ==================== Event 测试 ====================
    
    @Test(timeout = 4000)
    public void testEvent() {
        Event event = new Event(EventType.EMERGENCY, "data");
        assertEquals(EventType.EMERGENCY, event.getType());
        assertEquals("data", event.getData());
    }

    @Test(timeout = 4000)
    public void testEventNull() {
        Event event = new Event(EventType.ELEVATOR_FAULT, null);
        assertEquals(EventType.ELEVATOR_FAULT, event.getType());
        assertNull(event.getData());
    }

    // ==================== Floor 测试 ====================
    
    @Test(timeout = 4000)
    public void testFloorConstructor() {
        Floor floor = new Floor(5);
        assertEquals(5, floor.getFloorNumber());
    }

    @Test(timeout = 4000)
    public void testFloorAddRequestUp() {
        Floor floor = new Floor(3);
        PassengerRequest req = new PassengerRequest(3, 8, Priority.HIGH, RequestType.STANDARD);
        floor.addRequest(req);
        List<PassengerRequest> list = floor.getRequests(Direction.UP);
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testFloorAddRequestDown() {
        Floor floor = new Floor(8);
        PassengerRequest req = new PassengerRequest(8, 2, Priority.MEDIUM, RequestType.STANDARD);
        floor.addRequest(req);
        List<PassengerRequest> list = floor.getRequests(Direction.DOWN);
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testFloorGetRequestsClearsQueue() {
        Floor floor = new Floor(5);
        PassengerRequest r1 = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        floor.addRequest(r1);
        floor.getRequests(Direction.UP);
        List<PassengerRequest> empty = floor.getRequests(Direction.UP);
        assertEquals(0, empty.size());
    }

    // ==================== Elevator 核心测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorConstructor() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        assertEquals(1, elevator.getId());
        assertEquals(1, elevator.getCurrentFloor());
        assertEquals(Direction.UP, elevator.getDirection());
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
        assertEquals(ElevatorMode.NORMAL, elevator.getMode());
        assertEquals(0.0, elevator.getCurrentLoad(), 0.001);
        assertEquals(0.0, elevator.getEnergyConsumption(), 0.001);
        assertNotNull(elevator.getDestinationSet());
        assertNotNull(elevator.getPassengerList());
        assertNotNull(elevator.getLock());
        assertNotNull(elevator.getCondition());
        assertNotNull(elevator.getScheduler());
        assertNotNull(elevator.getObservers());
        assertTrue(elevator.getMaxLoad() > 0);
    }

    @Test(timeout = 4000)
    public void testElevatorSetters() {
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
        
        elevator.setEnergyConsumption(25.0);
        assertEquals(25.0, elevator.getEnergyConsumption(), 0.001);
        
        elevator.setMode(ElevatorMode.ENERGY_SAVING);
        assertEquals(ElevatorMode.ENERGY_SAVING, elevator.getMode());
    }

    @Test(timeout = 4000)
    public void testElevatorAddDestination() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.addDestination(8);
        assertTrue(elevator.getDestinationSet().contains(8));
    }

    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionUp() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(3);
        elevator.addDestination(5);
        elevator.updateDirection();
        assertEquals(Direction.UP, elevator.getDirection());
    }

    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionDown() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(10);
        elevator.addDestination(3);
        elevator.updateDirection();
        assertEquals(Direction.DOWN, elevator.getDirection());
    }

    @Test(timeout = 4000)
    public void testElevatorUpdateDirectionIdle() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.updateDirection();
        assertEquals(ElevatorStatus.IDLE, elevator.getStatus());
    }

    @Test(timeout = 4000)
    public void testElevatorUnloadPassengers() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(8);
        elevator.unloadPassengers();
        assertEquals(0, elevator.getPassengerList().size());
    }

    @Test(timeout = 4000)
    public void testElevatorClearAllRequests() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.addDestination(5);
        List<PassengerRequest> cleared = elevator.clearAllRequests();
        assertNotNull(cleared);
        assertTrue(elevator.getDestinationSet().isEmpty());
    }

    @Test(timeout = 4000)
    public void testElevatorHandleEmergency() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.addDestination(8);
        elevator.handleEmergency();
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
        assertTrue(elevator.getDestinationSet().contains(1));
    }

    @Test(timeout = 4000)
    public void testElevatorAddObserver() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        Observer obs = new Observer() {
            public void update(Observable o, Object arg) {}
        };
        elevator.addObserver(obs);
        assertTrue(elevator.getObservers().contains(obs));
    }

    @Test(timeout = 4000)
    public void testElevatorNotifyObservers() {
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        final boolean[] called = {false};
        Observer obs = new Observer() {
            public void update(Observable o, Object arg) {
                called[0] = true;
            }
        };
        elevator.addObserver(obs);
        Event event = new Event(EventType.EMERGENCY, "test");
        elevator.notifyObservers(event);
        assertTrue(called[0]);
    }

    // ==================== Scheduler 测试 ====================
    
    @Test(timeout = 4000)
    public void testSchedulerConstructor() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        assertNotNull(scheduler);
    }

    @Test(timeout = 4000)
    public void testSchedulerGetInstance() {
        Scheduler s1 = Scheduler.getInstance();
        Scheduler s2 = Scheduler.getInstance();
        assertNotNull(s1);
        assertSame(s1, s2);
    }

    @Test(timeout = 4000)
    public void testSchedulerSubmitRequestHigh() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        elevators.add(elevator);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(req);
    }

    @Test(timeout = 4000)
    public void testSchedulerSubmitRequestMedium() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(req);
    }

    @Test(timeout = 4000)
    public void testSchedulerUpdateFault() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        Elevator e1 = new Elevator(1, scheduler);
        Elevator e2 = new Elevator(2, scheduler);
        elevators.add(e1);
        elevators.add(e2);
        
        Event event = new Event(EventType.ELEVATOR_FAULT, null);
        scheduler.update(e1, event);
    }

    @Test(timeout = 4000)
    public void testSchedulerUpdateEmergency() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        
        Event event = new Event(EventType.EMERGENCY, null);
        scheduler.update(elevator, event);
        assertEquals(ElevatorStatus.EMERGENCY, elevator.getStatus());
    }

    @Test(timeout = 4000)
    public void testSchedulerUpdateOther() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        
        Event event = new Event(EventType.CONFIG_UPDATED, null);
        scheduler.update(elevator, event);
    }

    @Test(timeout = 4000)
    public void testSchedulerSetDispatchStrategy() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 10, new NearestElevatorStrategy());
        scheduler.setDispatchStrategy(new HighEfficiencyStrategy());
    }

    // ==================== NearestElevatorStrategy 测试 ====================
    
    @Test(timeout = 4000)
    public void testNearestStrategySelectElevator() {
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setCurrentFloor(5);
        e1.setStatus(ElevatorStatus.IDLE);
        
        Elevator e2 = new Elevator(2, scheduler);
        e2.setCurrentFloor(10);
        e2.setStatus(ElevatorStatus.IDLE);
        
        PassengerRequest req = new PassengerRequest(7, 15, Priority.HIGH, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(e1, e2), req);
        assertEquals(e1, chosen);
    }

    @Test(timeout = 4000)
    public void testNearestStrategyIsEligibleIdle() {
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.IDLE);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        assertTrue(strategy.isEligible(elevator, req));
    }

    @Test(timeout = 4000)
    public void testNearestStrategyIsEligibleMovingSame() {
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.UP);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        assertTrue(strategy.isEligible(elevator, req));
    }

    @Test(timeout = 4000)
    public void testNearestStrategyIsEligibleMovingDifferent() {
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MOVING);
        elevator.setDirection(Direction.DOWN);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        assertFalse(strategy.isEligible(elevator, req));
    }

    @Test(timeout = 4000)
    public void testNearestStrategyIsEligibleOther() {
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setStatus(ElevatorStatus.MAINTENANCE);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        assertFalse(strategy.isEligible(elevator, req));
    }

    @Test(timeout = 4000)
    public void testNearestStrategyNoEligible() {
        NearestElevatorStrategy strategy = new NearestElevatorStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.MAINTENANCE);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        assertNull(strategy.selectElevator(Arrays.asList(e1), req));
    }

    // ==================== HighEfficiencyStrategy 测试 ====================
    
    @Test(timeout = 4000)
    public void testHighEfficiencySelectElevator() {
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setCurrentFloor(5);
        e1.setStatus(ElevatorStatus.IDLE);
        
        Elevator e2 = new Elevator(2, scheduler);
        e2.setCurrentFloor(10);
        e2.setStatus(ElevatorStatus.IDLE);
        
        PassengerRequest req = new PassengerRequest(7, 15, Priority.HIGH, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(e1, e2), req);
        assertEquals(e1, chosen);
    }

    @Test(timeout = 4000)
    public void testHighEfficiencyIsCloserTrue() {
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator candidate = new Elevator(1, scheduler);
        candidate.setCurrentFloor(6);
        
        Elevator current = new Elevator(2, scheduler);
        current.setCurrentFloor(12);
        
        PassengerRequest req = new PassengerRequest(8, 15, Priority.HIGH, RequestType.STANDARD);
        assertTrue(strategy.isCloser(candidate, current, req));
    }

    @Test(timeout = 4000)
    public void testHighEfficiencyIsCloserFalse() {
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator candidate = new Elevator(1, scheduler);
        candidate.setCurrentFloor(15);
        
        Elevator current = new Elevator(2, scheduler);
        current.setCurrentFloor(7);
        
        PassengerRequest req = new PassengerRequest(8, 15, Priority.HIGH, RequestType.STANDARD);
        assertFalse(strategy.isCloser(candidate, current, req));
    }

    @Test(timeout = 4000)
    public void testHighEfficiencyNoElevator() {
        HighEfficiencyStrategy strategy = new HighEfficiencyStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.MAINTENANCE);
        e1.setDirection(Direction.DOWN);
        
        PassengerRequest req = new PassengerRequest(8, 15, Priority.HIGH, RequestType.STANDARD);
        assertNull(strategy.selectElevator(Arrays.asList(e1), req));
    }

    // ==================== EnergySavingStrategy 测试 ====================
    
    @Test(timeout = 4000)
    public void testEnergySavingSelectIdle() {
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator idle = new Elevator(1, scheduler);
        idle.setStatus(ElevatorStatus.IDLE);
        
        Elevator moving = new Elevator(2, scheduler);
        moving.setStatus(ElevatorStatus.MOVING);
        
        PassengerRequest req = new PassengerRequest(5, 10, Priority.HIGH, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(moving, idle), req);
        assertEquals(idle, chosen);
    }

    @Test(timeout = 4000)
    public void testEnergySavingSelectClose() {
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.MOVING);
        e1.setDirection(Direction.UP);
        e1.setCurrentFloor(9);
        
        PassengerRequest req = new PassengerRequest(10, 15, Priority.HIGH, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(e1), req);
        assertEquals(e1, chosen);
    }

    @Test(timeout = 4000)
    public void testEnergySavingSelectFar() {
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.MOVING);
        e1.setDirection(Direction.UP);
        e1.setCurrentFloor(2);
        
        PassengerRequest req = new PassengerRequest(10, 15, Priority.HIGH, RequestType.STANDARD);
        assertNull(strategy.selectElevator(Arrays.asList(e1), req));
    }

    @Test(timeout = 4000)
    public void testEnergySavingWrongDirection() {
        EnergySavingStrategy strategy = new EnergySavingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.MOVING);
        e1.setDirection(Direction.DOWN);
        e1.setCurrentFloor(8);
        
        PassengerRequest req = new PassengerRequest(10, 15, Priority.HIGH, RequestType.STANDARD);
        assertNull(strategy.selectElevator(Arrays.asList(e1), req));
    }

    // ==================== PredictiveSchedulingStrategy 测试 ====================
    
    @Test(timeout = 4000)
    public void testPredictiveSelectElevator() {
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setCurrentFloor(5);
        
        Elevator e2 = new Elevator(2, scheduler);
        e2.setCurrentFloor(15);
        
        PassengerRequest req = new PassengerRequest(8, 15, Priority.HIGH, RequestType.STANDARD);
        Elevator chosen = strategy.selectElevator(Arrays.asList(e1, e2), req);
        assertEquals(e1, chosen);
    }

    @Test(timeout = 4000)
    public void testPredictiveCalculateCost() {
        PredictiveSchedulingStrategy strategy = new PredictiveSchedulingStrategy();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, strategy);
        Elevator elevator = new Elevator(1, scheduler);
        elevator.setCurrentFloor(5);
        
        PassengerRequest req = new PassengerRequest(10, 15, Priority.HIGH, RequestType.STANDARD);
        double cost = strategy.calculatePredictedCost(elevator, req);
        assertTrue(cost > 0);
    }

    // ==================== ElevatorManager 测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorManagerSingleton() {
        ElevatorManager m1 = ElevatorManager.getInstance();
        ElevatorManager m2 = ElevatorManager.getInstance();
        assertNotNull(m1);
        assertSame(m1, m2);
    }

    @Test(timeout = 4000)
    public void testElevatorManagerConstructor() {
        ElevatorManager manager = new ElevatorManager();
        assertNotNull(manager);
    }

    @Test(timeout = 4000)
    public void testElevatorManagerRegister() {
        ElevatorManager manager = ElevatorManager.getInstance();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(99, scheduler);
        manager.registerElevator(elevator);
        assertEquals(elevator, manager.getElevatorById(99));
    }

    @Test(timeout = 4000)
    public void testElevatorManagerGetAll() {
        ElevatorManager manager = ElevatorManager.getInstance();
        Collection<Elevator> all = manager.getAllElevators();
        assertNotNull(all);
    }

    // ==================== NotificationService 测试 ====================
    
    @Test(timeout = 4000)
    public void testNotificationServiceSingleton() {
        NotificationService s1 = NotificationService.getInstance();
        NotificationService s2 = NotificationService.getInstance();
        assertNotNull(s1);
        assertSame(s1, s2);
    }

    @Test(timeout = 4000)
    public void testNotificationServiceConstructor() {
        NotificationService service = new NotificationService();
        assertNotNull(service);
    }

    @Test(timeout = 4000)
    public void testNotificationServiceSendEmergency() {
        NotificationService service = NotificationService.getInstance();
        NotificationService.Notification notif = new NotificationService.Notification(
            NotificationService.NotificationType.EMERGENCY,
            "Emergency",
            Arrays.asList("user@test.com")
        );
        service.sendNotification(notif);
    }

    @Test(timeout = 4000)
    public void testNotificationServiceSendMaintenance() {
        NotificationService service = NotificationService.getInstance();
        NotificationService.Notification notif = new NotificationService.Notification(
            NotificationService.NotificationType.MAINTENANCE,
            "Maintenance",
            Arrays.asList("admin@test.com")
        );
        service.sendNotification(notif);
    }

    @Test(timeout = 4000)
    public void testNotificationServiceSendInfo() {
        NotificationService service = NotificationService.getInstance();
        NotificationService.Notification notif = new NotificationService.Notification(
            NotificationService.NotificationType.INFORMATION,
            "Info",
            Arrays.asList("user@test.com")
        );
        service.sendNotification(notif);
    }

    @Test(timeout = 4000)
    public void testNotificationObject() {
        NotificationService.Notification notif = new NotificationService.Notification(
            NotificationService.NotificationType.SYSTEM_UPDATE,
            "Update",
            Arrays.asList("user1@test.com", "user2@test.com")
        );
        assertEquals(NotificationService.NotificationType.SYSTEM_UPDATE, notif.getType());
        assertEquals("Update", notif.getMessage());
        assertEquals(2, notif.getRecipients().size());
    }

    @Test(timeout = 4000)
    public void testSMSChannelSupports() {
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        assertTrue(channel.supports(NotificationService.NotificationType.EMERGENCY));
        assertTrue(channel.supports(NotificationService.NotificationType.MAINTENANCE));
        assertFalse(channel.supports(NotificationService.NotificationType.INFORMATION));
        assertFalse(channel.supports(NotificationService.NotificationType.SYSTEM_UPDATE));
    }

    @Test(timeout = 4000)
    public void testSMSChannelSend() {
        NotificationService.SMSChannel channel = new NotificationService.SMSChannel();
        NotificationService.Notification notif = new NotificationService.Notification(
            NotificationService.NotificationType.EMERGENCY,
            "Test",
            Arrays.asList("123456")
        );
        channel.send(notif);
    }

    @Test(timeout = 4000)
    public void testEmailChannelSupports() {
        NotificationService.EmailChannel channel = new NotificationService.EmailChannel();
        assertTrue(channel.supports(NotificationService.NotificationType.EMERGENCY));
        assertTrue(channel.supports(NotificationService.NotificationType.MAINTENANCE));
        assertTrue(channel.supports(NotificationService.NotificationType.INFORMATION));
        assertTrue(channel.supports(NotificationService.NotificationType.SYSTEM_UPDATE));
    }

    @Test(timeout = 4000)
    public void testEmailChannelSend() {
        NotificationService.EmailChannel channel = new NotificationService.EmailChannel();
        NotificationService.Notification notif = new NotificationService.Notification(
            NotificationService.NotificationType.INFORMATION,
            "Test",
            Arrays.asList("test@test.com")
        );
        channel.send(notif);
    }

    // ==================== LogManager 测试 ====================
    
    @Test(timeout = 4000)
    public void testLogManagerSingleton() {
        LogManager m1 = LogManager.getInstance();
        LogManager m2 = LogManager.getInstance();
        assertNotNull(m1);
        assertSame(m1, m2);
    }

    @Test(timeout = 4000)
    public void testLogManagerConstructor() {
        LogManager manager = new LogManager();
        assertNotNull(manager);
    }

    @Test(timeout = 4000)
    public void testLogManagerRecordElevatorEvent() {
        LogManager manager = LogManager.getInstance();
        manager.recordElevatorEvent(1, "Started");
    }

    @Test(timeout = 4000)
    public void testLogManagerRecordSchedulerEvent() {
        LogManager manager = LogManager.getInstance();
        manager.recordSchedulerEvent("Initialized");
    }

    @Test(timeout = 4000)
    public void testLogManagerRecordEvent() {
        LogManager manager = LogManager.getInstance();
        manager.recordEvent("Test", "Message");
    }

    @Test(timeout = 4000)
    public void testLogManagerQueryLogs() {
        LogManager manager = LogManager.getInstance();
        long start = System.currentTimeMillis();
        manager.recordEvent("Source1", "Msg1");
        manager.recordEvent("Source1", "Msg2");
        manager.recordEvent("Source2", "Msg3");
        long end = System.currentTimeMillis() + 1000;
        
        List<LogManager.SystemLog> logs = manager.queryLogs("Source1", start, end);
        assertTrue(logs.size() >= 2);
    }

    @Test(timeout = 4000)
    public void testSystemLog() {
        LogManager.SystemLog log = new LogManager.SystemLog("Test", "Message", 12345L);
        assertEquals("Test", log.getSource());
        assertEquals("Message", log.getMessage());
        assertEquals(12345L, log.getTimestamp());
    }

    // ==================== AnalyticsEngine 测试 ====================
    
    @Test(timeout = 4000)
    public void testAnalyticsEngineSingleton() {
        AnalyticsEngine e1 = AnalyticsEngine.getInstance();
        AnalyticsEngine e2 = AnalyticsEngine.getInstance();
        assertNotNull(e1);
        assertSame(e1, e2);
    }

    @Test(timeout = 4000)
    public void testAnalyticsEngineConstructor() {
        AnalyticsEngine engine = new AnalyticsEngine();
        assertNotNull(engine);
    }

    @Test(timeout = 4000)
    public void testAnalyticsEngineProcessReport() {
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        ElevatorStatusReport report = new ElevatorStatusReport(
            1, 5, Direction.UP, ElevatorStatus.MOVING, 2.0, 350.0, 5
        );
        engine.processStatusReport(report);
    }

    @Test(timeout = 4000)
    public void testAnalyticsEngineUpdateCount() {
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        engine.updateFloorPassengerCount(5, 10);
        engine.updateFloorPassengerCount(8, 15);
    }

    @Test(timeout = 4000)
    public void testAnalyticsEngineIsPeakHoursTrue() {
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        engine.updateFloorPassengerCount(1, 30);
        engine.updateFloorPassengerCount(2, 25);
        assertTrue(engine.isPeakHours());
    }

    @Test(timeout = 4000)
    public void testAnalyticsEngineGenerateReport() {
        AnalyticsEngine engine = AnalyticsEngine.getInstance();
        AnalyticsEngine.Report report = engine.generatePerformanceReport();
        assertNotNull(report);
        assertEquals("System Performance Report", report.getTitle());
        assertTrue(report.getGeneratedTime() > 0);
    }

    @Test(timeout = 4000)
    public void testAnalyticsReport() {
        AnalyticsEngine.Report report = new AnalyticsEngine.Report("Test Report", 12345L);
        assertEquals("Test Report", report.getTitle());
        assertEquals(12345L, report.getGeneratedTime());
    }

    // ==================== ElevatorStatusReport 测试 ====================
    
    @Test(timeout = 4000)
    public void testElevatorStatusReport() {
        ElevatorStatusReport report = new ElevatorStatusReport(
            1, 5, Direction.UP, ElevatorStatus.MOVING, 2.5, 420.0, 6
        );
        assertEquals(1, report.getElevatorId());
        assertEquals(5, report.getCurrentFloor());
        assertEquals(Direction.UP, report.getDirection());
        assertEquals(ElevatorStatus.MOVING, report.getStatus());
        assertEquals(2.5, report.getSpeed(), 0.001);
        assertEquals(420.0, report.getCurrentLoad(), 0.001);
        assertEquals(6, report.getPassengerCount());
    }

    @Test(timeout = 4000)
    public void testElevatorStatusReportToString() {
        ElevatorStatusReport report = new ElevatorStatusReport(
            2, 10, Direction.DOWN, ElevatorStatus.STOPPED, 0.0, 350.0, 5
        );
        String str = report.toString();
        assertNotNull(str);
        assertTrue(str.contains("2"));
        assertTrue(str.contains("10"));
    }

    // ==================== MaintenanceManager 测试 ====================
    
    @Test(timeout = 4000)
    public void testMaintenanceManagerSingleton() {
        MaintenanceManager m1 = MaintenanceManager.getInstance();
        MaintenanceManager m2 = MaintenanceManager.getInstance();
        assertNotNull(m1);
        assertSame(m1, m2);
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerSchedule() {
        MaintenanceManager manager = MaintenanceManager.getInstance();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        manager.scheduleMaintenance(elevator);
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerOnEvent() {
        MaintenanceManager manager = MaintenanceManager.getInstance();
        Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        EventBus.Event event = new EventBus.Event(EventType.ELEVATOR_FAULT, elevator);
        manager.onEvent(event);
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerOnEventOther() {
        MaintenanceManager manager = MaintenanceManager.getInstance();
        EventBus.Event event = new EventBus.Event(EventType.CONFIG_UPDATED, "config");
        manager.onEvent(event);
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerPerform() {
        MaintenanceManager manager = MaintenanceManager.getInstance();
        MaintenanceManager.MaintenanceTask task = new MaintenanceManager.MaintenanceTask(
            1, System.currentTimeMillis(), "Repair"
        );
        manager.performMaintenance(task);
    }

    @Test(timeout = 4000)
    public void testMaintenanceManagerRecord() {
        MaintenanceManager manager = MaintenanceManager.getInstance();
        manager.recordMaintenanceResult(1, "Success");
    }

    @Test(timeout = 4000)
    public void testMaintenanceTask() {
        MaintenanceManager.MaintenanceTask task = new MaintenanceManager.MaintenanceTask(
            1, 12345L, "Fault repair"
        );
        assertEquals(1, task.getElevatorId());
        assertEquals(12345L, task.getScheduledTime());
        assertEquals("Fault repair", task.getDescription());
    }

    @Test(timeout = 4000)
    public void testMaintenanceRecord() {
        MaintenanceManager.MaintenanceRecord record = new MaintenanceManager.MaintenanceRecord(
            1, 12345L, "Completed"
        );
        assertEquals(1, record.getElevatorId());
        assertEquals(12345L, record.getMaintenanceTime());
        assertEquals("Completed", record.getResult());
    }

    // ==================== SecurityMonitor 测试 ====================
    
    @Test(timeout = 4000)
    public void testSecurityMonitorSingleton() {
        SecurityMonitor m1 = SecurityMonitor.getInstance();
        SecurityMonitor m2 = SecurityMonitor.getInstance();
        assertNotNull(m1);
        assertSame(m1, m2);
    }

    @Test(timeout = 4000)
    public void testSecurityMonitorHandleEmergency() {
        SecurityMonitor monitor = SecurityMonitor.getInstance();
        monitor.handleEmergency("Test emergency");
    }

    @Test(timeout = 4000)
    public void testSecurityMonitorOnEventEmergency() {
        SecurityMonitor monitor = SecurityMonitor.getInstance();
        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, "Emergency data");
        monitor.onEvent(event);
    }

    @Test(timeout = 4000)
    public void testSecurityMonitorOnEventOther() {
        SecurityMonitor monitor = SecurityMonitor.getInstance();
        EventBus.Event event = new EventBus.Event(EventType.CONFIG_UPDATED, "config");
        monitor.onEvent(event);
    }

    @Test(timeout = 4000)
    public void testSecurityEvent() {
        SecurityMonitor.SecurityEvent event = new SecurityMonitor.SecurityEvent(
            "Test event", 12345L, "data"
        );
        assertEquals("Test event", event.getDescription());
        assertEquals(12345L, event.getTimestamp());
        assertEquals("data", event.getData());
    }

    // ==================== ThreadPoolManager 测试 ====================
    
    @Test(timeout = 4000)
    public void testThreadPoolManagerSingleton() {
        ThreadPoolManager m1 = ThreadPoolManager.getInstance();
        ThreadPoolManager m2 = ThreadPoolManager.getInstance();
        assertNotNull(m1);
        assertSame(m1, m2);
    }

    @Test(timeout = 4000)
    public void testThreadPoolManagerConstructor() {
        ThreadPoolManager manager = new ThreadPoolManager();
        assertNotNull(manager);
        manager.shutdown();
    }

    @Test(timeout = 4000)
    public void testThreadPoolManagerSubmitTask() {
        ThreadPoolManager manager = new ThreadPoolManager();
        final boolean[] executed = {false};
        manager.submitTask(new Runnable() {
            public void run() {
                executed[0] = true;
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        manager.shutdown();
        assertTrue(executed[0]);
    }

    // ==================== EventBus 测试 ====================
    
    @Test(timeout = 4000)
    public void testEventBusSingleton() {
        EventBus b1 = EventBus.getInstance();
        EventBus b2 = EventBus.getInstance();
        assertNotNull(b1);
        assertSame(b1, b2);
    }

    @Test(timeout = 4000)
    public void testEventBusConstructor() {
        EventBus bus = new EventBus();
        assertNotNull(bus);
    }

    @Test(timeout = 4000)
    public void testEventBusSubscribe() {
        EventBus bus = EventBus.getInstance();
        EventBus.EventListener listener = new EventBus.EventListener() {
            public void onEvent(EventBus.Event event) {}
        };
        bus.subscribe(EventType.CONFIG_UPDATED, listener);
    }

    @Test(timeout = 4000)
    public void testEventBusPublish() {
        EventBus bus = EventBus.getInstance();
        final boolean[] called = {false};
        EventBus.EventListener listener = new EventBus.EventListener() {
            public void onEvent(EventBus.Event event) {
                called[0] = true;
            }
        };
        bus.subscribe(EventType.MAINTENANCE_REQUIRED, listener);
        EventBus.Event event = new EventBus.Event(EventType.MAINTENANCE_REQUIRED, "data");
        bus.publish(event);
        assertTrue(called[0]);
    }

    @Test(timeout = 4000)
    public void testEventBusPublishNoListener() {
        EventBus bus = EventBus.getInstance();
        EventBus.Event event = new EventBus.Event(EventType.CONFIG_UPDATED, "data");
        bus.publish(event);
    }

    @Test(timeout = 4000)
    public void testEventBusEvent() {
        EventBus.Event event = new EventBus.Event(EventType.EMERGENCY, "emergency data");
        assertEquals(EventType.EMERGENCY, event.getType());
        assertEquals("emergency data", event.getData());
    }

    // ==================== 综合测试 ====================
    
    @Test(timeout = 4000)
    public void testIntegration1() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 20, new NearestElevatorStrategy());
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.IDLE);
        e1.setCurrentFloor(1);
        elevators.add(e1);
        
        PassengerRequest req = new PassengerRequest(5, 15, Priority.HIGH, RequestType.STANDARD);
        scheduler.submitRequest(req);
        
        assertTrue(e1.getDestinationSet().contains(5));
    }

    @Test(timeout = 4000)
    public void testIntegration2() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 20, new HighEfficiencyStrategy());
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.IDLE);
        elevators.add(e1);
        
        PassengerRequest req = new PassengerRequest(3, 8, Priority.MEDIUM, RequestType.STANDARD);
        scheduler.submitRequest(req);
    }

    @Test(timeout = 4000)
    public void testIntegration3() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 20, new EnergySavingStrategy());
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.IDLE);
        elevators.add(e1);
        
        PassengerRequest req = new PassengerRequest(2, 9, Priority.LOW, RequestType.STANDARD);
        scheduler.submitRequest(req);
    }

    @Test(timeout = 4000)
    public void testIntegration4() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 20, new PredictiveSchedulingStrategy());
        
        Elevator e1 = new Elevator(1, scheduler);
        e1.setStatus(ElevatorStatus.IDLE);
        elevators.add(e1);
        
        PassengerRequest req = new PassengerRequest(4, 12, Priority.HIGH, RequestType.DESTINATION_CONTROL);
        scheduler.submitRequest(req);
    }

    @Test(timeout = 4000)
    public void testMoveDown() {
        try {
            Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
            Elevator elevator = new Elevator(1, scheduler);
            elevator.setCurrentFloor(10);
            elevator.setDirection(Direction.DOWN);
            elevator.addDestination(5);
            elevator.move();
            assertEquals(9, elevator.getCurrentFloor());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test(timeout = 4000)
    public void testMoveUp() {
        try {
            Scheduler scheduler = new Scheduler(new ArrayList<>(), 10, new NearestElevatorStrategy());
            Elevator elevator = new Elevator(1, scheduler);
            elevator.setCurrentFloor(5);
            elevator.setDirection(Direction.UP);
            elevator.addDestination(8);
            elevator.move();
            assertEquals(6, elevator.getCurrentFloor());
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test(timeout = 4000)
    public void testLoadPassengers() {
        List<Elevator> elevators = new ArrayList<>();
        Scheduler scheduler = new Scheduler(elevators, 20, new NearestElevatorStrategy());
        Elevator elevator = new Elevator(1, scheduler);
        elevators.add(elevator);
        elevator.setCurrentFloor(5);
        elevator.setDirection(Direction.UP);
        elevator.setCurrentLoad(0.0);
        elevator.loadPassengers();
    }
}
