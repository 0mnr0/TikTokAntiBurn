package made.by.human.tiktokantiburn;

public class BlockInfo {
    public static final int DEFAULT_HIDE_TIME = 5000;
    public static final int HIDE_TIME_UNSET = 0;

    public int width;
    public int height;
    public int x;
    public int y;
    public long radius = 0;
    public float alpha = 1f;
    public int hideTime = HIDE_TIME_UNSET;

    public BlockInfo(int width, int height, int x, int y, float alpha, long radius, int hideTime) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.alpha = alpha>0.999 ? 1 : alpha; // just safe feature
        this.hideTime = hideTime;
        this.radius = radius;
    }

    public BlockInfo() {}
}

