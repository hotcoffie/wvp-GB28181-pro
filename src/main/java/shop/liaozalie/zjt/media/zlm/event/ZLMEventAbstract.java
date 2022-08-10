package shop.liaozalie.zjt.media.zlm.event;

import org.springframework.context.ApplicationEvent;

public abstract class ZLMEventAbstract extends ApplicationEvent {


    private static final long serialVersionUID = 1L;

    private String mediaServerId;


    public ZLMEventAbstract(Object source) {
        super(source);
    }

    public String getMediaServerId() {
        return mediaServerId;
    }

    public void setMediaServerId(String mediaServerId) {
        this.mediaServerId = mediaServerId;
    }
}
