package shop.liaozalie.zjt.gb28181.task;

import javax.sip.DialogState;

/**
 * @author lin
 */
public interface ISubscribeTask extends Runnable{
    void stop();

    DialogState getDialogState();
}
