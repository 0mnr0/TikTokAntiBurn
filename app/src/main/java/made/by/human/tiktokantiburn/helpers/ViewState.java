package made.by.human.tiktokantiburn.helpers;

public class ViewState {
    public float alpha;
    public int visibility;
    public int keepOnEveryVideo;

    public ViewState(float alpha, int visibility, boolean keepOnEveryVideo) {
        this.alpha = alpha;
        this.visibility = visibility;
        this.keepOnEveryVideo = keepOnEveryVideo ? 1 : 0;
    }

}
