package milen.com.gentleservice.api.objects_model;

public class Compliment {
    private int _id;
    private String content;
    private boolean isLoaded;
    private boolean isCustom;
    private boolean isHated;

    public Compliment(int id, String content, boolean isLoaded, boolean isCustom, boolean isHated) {
        setId(id);
        setContent(content);
        setIsLoaded(isLoaded);
        setIsCustom(isCustom);
        setIsHated(isHated);
    }

    public Compliment(String content) {
        this((int) (System.currentTimeMillis() /1000), content, false, false, false);
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setIsLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public boolean isHated() {
        return isHated;
    }

    public void setIsHated(boolean isHated) {
        this.isHated = isHated;
    }
}
