package me.dustin.jex.load.impl;

public interface IPlayerMoveC2SPacket {

    double getX();

    void setX(double x);

    double getY();

    void setY(double y);

    double getZ();

    void setZ(double z);

    float getYaw();

    void setYaw(float z);

    float getPitch();

    void setPitch(float z);

    boolean getOnGround();

    void setOnGround(boolean onGround);

}
