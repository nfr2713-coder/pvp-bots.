package ru.pvpbots;

import java.util.ArrayList;
import java.util.List;

public class Store {
    public List<BotData> bots = new ArrayList<>();
    public double pullSpeed = 50;      // блоков в секунду, 1..500
    public double maceLaunch = 1.5;    // вертикальная скорость подброса (блоков/тик)
    public boolean maceEnabled = true;
}
