package core;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import utility.GBUtility;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class ActionDisplay implements NativeMouseListener, NativeKeyListener {
    private HashSet<Long> actions = new HashSet<>();
    private boolean printAPMFlag = true;
    private int lastPressCode = 0;

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
                HashSet<Long> removalSet = new HashSet<>();
                for (Long action : actions) {
                    if (action + 15000 > System.currentTimeMillis()) APM++;
                    else removalSet.add(action);
                }
                actions.removeAll(removalSet);

                GBUtility.writeTextToFile("APM: " + APM * 4, "output/APM.txt", false);
                //System.out.println("APM: " + APM * 6);
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
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
        if (nativeMouseEvent.getButton() != lastPressCode) {
            actions.add(System.currentTimeMillis());
            lastPressCode = nativeMouseEvent.getButton();
        }

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        //System.out.println("Mouse Release: " + nativeMouseEvent.toString());
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        //System.out.println("Key Pressed: " + nativeKeyEvent.getKeyCode());
        if (nativeKeyEvent.getKeyCode() == 57) return;
        if (nativeKeyEvent.getKeyCode() != lastPressCode) {
            actions.add(System.currentTimeMillis());
            lastPressCode = nativeKeyEvent.getKeyCode();
        }

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }
}
