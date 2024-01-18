package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.exception.CityNotKnownException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.NoPathToDestinationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SequencedCollection;

public class RideRight implements ItineraryPlanner {
    private final List<Journey> schedule;
    private final List<City> cities;

    private final int moneyValue = 20;
    private final int meterInKm = 1000;

    public RideRight(List<Journey> schedule) {
        this.schedule = schedule;
        cities = new ArrayList<>();
        for (Journey journey : schedule) {
            if (!cities.contains(journey.from())) {
                cities.add(journey.from());
            }
            if (!cities.contains(journey.to())) {
                cities.add(journey.to());
            }
        }
    }

    private BigDecimal getPrivilege(Journey journey) { //gscore
        return journey.price().add(journey.vehicleType().getGreenTax().multiply(journey.price()));
    }

    private BigDecimal getPrivilegeToEnd(City city, City end) { //hscore
        return new BigDecimal((city.location().getDistanceTo(end.location()) / meterInKm) * moneyValue);
    }

    private Journey pickByPrivilege(SequencedCollection<Journey> arr) {

        Journey answer = null;
        BigDecimal smallestPrivilege = new BigDecimal(Integer.MAX_VALUE);
        for (Journey journey : arr) {
            BigDecimal temp = getPrivilege(journey);
            if (smallestPrivilege.compareTo(temp) > 0) {
                smallestPrivilege = temp;
                answer = journey;
            }
        }
        return answer;
    }

    private SequencedCollection<Journey> createPathFromCities(LinkedHashMap<City,
            Journey> arrivedFrom, City destination) {
        ArrayList<Journey> path = new ArrayList<>();
        City curr = destination;
        while (arrivedFrom.containsKey(curr)) {
            path.addFirst(arrivedFrom.get(curr));
            curr = path.getFirst().from();
        }

        return path;
    }

    private ArrayList<Journey> directRoute(City start, City destination) throws NoPathToDestinationException {
        SequencedCollection<Journey> temp = new ArrayList<>();

        for (Journey journey : schedule) {
            if (journey.from().equals(start) && journey.to().equals(destination)) {
                temp.addLast(journey);      //adding journey to a list
            }
        }
        if (temp.isEmpty()) {
            throw new NoPathToDestinationException();
        }
        return new ArrayList<Journey>(Collections.singleton(pickByPrivilege(temp)));
    }

    @Override
    public SequencedCollection<Journey> findCheapestPath(City start, City destination, boolean allowTransfer)
            throws CityNotKnownException, NoPathToDestinationException {
        if (!cities.contains(start) || !cities.contains(destination)) {
            throw new CityNotKnownException();
        }
        if (!allowTransfer) {   //checking if there is a direct route
            return directRoute(start, destination);
        } else {    //A* (A star) algorithm 
            LinkedHashMap<City, Journey> arrivedFrom = new LinkedHashMap<>();
            LinkedHashMap<City, BigDecimal> map = new LinkedHashMap<>();
            map.put(start, BigDecimal.ZERO);
            LinkedHashMap<City, BigDecimal> privilegeMap = new LinkedHashMap<>();
            privilegeMap.put(start, getPrivilegeToEnd(start, destination));
            PriorityQueue<City> queue = new PriorityQueue<>(cities.size(), new Comparator<City>() {
                @Override
                public int compare(City o1, City o2) {
                    if (!privilegeMap.containsKey(o1) && !privilegeMap.containsKey(o2)) {
                        return 0;
                    } else if (!privilegeMap.containsKey(o1)) {
                        return 1;
                    } else if (!privilegeMap.containsKey(o2)) {
                        return -1;
                    } else if (privilegeMap.get(o1).compareTo(privilegeMap.get(o2)) != 0) {
                        return privilegeMap.get(o1).compareTo(privilegeMap.get(o2));
                    } else {
                        return o1.name().compareTo(o2.name());
                    }
                }
            });
            queue.add(start);

            while (!queue.isEmpty()) {
                City currentKey = queue.poll();
                if (currentKey.name().equals(destination.name())) {
                    return createPathFromCities(arrivedFrom, destination);
                }
                for (Journey journey : schedule) {
                    if (journey.from().equals(currentKey)) {
                        BigDecimal currPrivilege = map.get(currentKey).add(getPrivilege(journey));
                        if (!map.containsKey(journey.to()) || currPrivilege.compareTo(map.get(journey.to())) < 0) {
                            arrivedFrom.put(journey.to(), journey);
                            map.put(journey.to(), currPrivilege);
                            privilegeMap.put(journey.to(),
                                    currPrivilege.add(getPrivilegeToEnd(journey.to(), destination)));
                            if (!queue.contains(journey.to())) {
                                queue.add(journey.to());
                            }
                        }
                    }
                }
            }
            throw new NoPathToDestinationException();
        }
    }
}