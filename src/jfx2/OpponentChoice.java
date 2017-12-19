package jfx2;

class OpponentChoice {
    static final int CLIENT = 0;
    static final int DIRECT = 1;
    static final int HOST = 2;
    static final int LOCAL = 3;
    final String host;
    final String display;
    final int type;

    OpponentChoice(String host, String display, int type) {
        this.host = host;
        this.display = display;
        this.type = type;
    }

    @Override
    public String toString() {
        return display;
    }
}