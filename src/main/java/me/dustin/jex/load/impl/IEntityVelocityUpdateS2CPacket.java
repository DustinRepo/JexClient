package me.dustin.jex.load.impl;

public interface IEntityVelocityUpdateS2CPacket {

    int getVelocityX();
    int getVelocityY();
    int getVelocityZ();

    void setVelocityX(int velocityX);
    void setVelocityY(int velocityY);
    void setVelocityZ(int velocityZ);

}
