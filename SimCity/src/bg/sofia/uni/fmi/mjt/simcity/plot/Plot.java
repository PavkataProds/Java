package bg.sofia.uni.fmi.mjt.simcity.plot;

import bg.sofia.uni.fmi.mjt.simcity.exception.BuildableAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.simcity.exception.BuildableNotFoundException;
import bg.sofia.uni.fmi.mjt.simcity.exception.InsufficientPlotAreaException;
import bg.sofia.uni.fmi.mjt.simcity.property.buildable.Buildable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Plot<E extends Buildable> implements PlotAPI {

    private final int buildableArea;
    private final Map<String, E> buildables;

    public Plot(int buildableArea) {
        this.buildableArea = buildableArea;
        buildables = new HashMap<String, E>();
    }

    @Override
    public void demolish(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException();
        }
        for (Map.Entry<String, E> entry : buildables.entrySet()) {
            if (entry.getKey().equals(address)) {
                buildables.remove(address);
                return;
            }
        }
        throw new BuildableNotFoundException();
    }

    @Override
    public void demolishAll() {
        buildables.clear();
    }

    @Override
    public Map getAllBuildables() {
        return Map.copyOf(this.buildables);
    }

    @Override
    public int getRemainingBuildableArea() {
        int result = buildableArea;
        for (Map.Entry<String, E> entry : buildables.entrySet()) {
            result -= entry.getValue().getArea();
        }
        return result;
    }

    @Override
    public void construct(String address, Buildable buildable) {
        if (address == null || address.isBlank() || buildable == null) {
            throw new IllegalArgumentException();
        }
        for (Map.Entry<String, E> entry : buildables.entrySet()) {
            if (entry.getKey().equals(address)) {
                throw new BuildableAlreadyExistsException();
            }
        }
        if (this.getRemainingBuildableArea() - buildable.getArea() < 0) {
            throw new InsufficientPlotAreaException();
        }
        buildables.put(address, (E) buildable);
    }

    @Override
    public void constructAll(Map buildables) {
        if (buildables == null || buildables.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (Map.Entry<String, E> entry : this.buildables.entrySet()) {
            buildables.forEach((key, value) -> {
                if (entry.getKey().equals(key)) {
                    throw new BuildableAlreadyExistsException();
                }
            });
        }
        AtomicInteger areaLeft = new AtomicInteger(this.getRemainingBuildableArea());
        buildables.forEach((key, value) -> {
            E newValue = (E) value;
            areaLeft.addAndGet(-newValue.getArea());
            if (areaLeft.get() < 0) {
                throw new InsufficientPlotAreaException();
            }
        });
        this.buildables.putAll(buildables);
    }
}