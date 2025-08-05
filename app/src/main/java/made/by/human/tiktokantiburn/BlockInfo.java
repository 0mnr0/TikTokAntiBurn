package made.by.human.tiktokantiburn;

public class BlockInfo {
    int width;
    int height;
    int x;
    int y;
    long radius = 0;

    public BlockInfo(int width, int height, int x, int y, long radius) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public BlockInfo() {}
}

