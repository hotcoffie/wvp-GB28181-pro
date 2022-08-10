package shop.liaozalie.zjt.common;

public class SystemInfoDto<T> {
    private String time;
    private T data;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
