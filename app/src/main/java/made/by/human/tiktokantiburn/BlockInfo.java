package made.by.human.tiktokantiburn;

public class BlockInfo {
    public int width;
    public int height;
    public int x;
    public int y;
    public long radius = 0;
    public float alpha = 1f;

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

