package bg.sofia.uni.fmi.mjt.itinerary;

public record Location(int x, int y) {
    public int getDistanceTo(Location other) {
        //Manhattan distance
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
}