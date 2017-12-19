package jfx2;

public interface NetworkMatch {
    void onConnecting();

    void onWaitingForNetwork();

    void onConnectedTo(String host);

    void onCannotConnectTo(String host, Throwable e);
}
