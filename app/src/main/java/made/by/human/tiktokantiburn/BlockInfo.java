package made.by.human.tiktokantiburn;

public class BlockInfo {
    int width;
    int height;
    int x;
    int y;
    long radius = 0;
    float alpha = 1f;

    public BlockInfo(int width, int height, int x, int y, float alpha, long radius) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.alpha = alpha>0.999 ? 1 : alpha; // just safe feature
        this.radius = radius;
    }

    public BlockInfo() {}
}

