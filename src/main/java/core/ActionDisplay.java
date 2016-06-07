package core;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseListener;

import java.awt.*;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by Dons on 07-06-2016.
 */
public class ActionDisplay implements NativeMouseListener, NativeKeyListener {
    private List<Long> actions = new ArrayList<>();
    private boolean printAPMFlag = true;

    public static void main(String[] args) throws NativeHookException {
        ActionDisplay apmCounter = new ActionDisplay();

        GlobalScreen.registerNativeHook();
        LogManager.getLogManager().getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.parse("OFF"));
        GlobalScreen.addNativeMouseListener(apmCounter);
        GlobalScreen.addNativeKeyListener(apmCounter);
        apmCounter.printAPM();
    }



    public void printAPM() {
        new Thread(() -> {
            while (printAPMFlag) {
                int APM = 0;
                for (Long action : actions) {
                    if (action + 10000 > System.currentTimeMillis()) APM++;
                }
                System.out.println("APM: " + APM * 6);
                try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        //System.out.println("Mouse clicked: " + nativeMouseEvent.toString());
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        //System.out.println("Mouse Pressed: " + nativeMouseEvent.getButton());
        actions.add(System.currentTimeMillis());
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        //System.out.println("Mouse Release: " + nativeMouseEvent.toString());
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        //System.out.println("Key Pressed: " + nativeKeyEvent.getKeyCode());
        actions.add(System.currentTimeMillis());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }
}
