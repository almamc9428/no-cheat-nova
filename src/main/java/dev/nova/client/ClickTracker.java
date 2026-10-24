package dev.nova.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** Counts physical mouse clicks for the HUD without synthesizing input. */
public final class ClickTracker {
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean wasLeftDown;
    private boolean wasRightDown;
    private boolean leftDown;
    private boolean rightDown;

    public void tick(Minecraft client) {
        long now = System.currentTimeMillis();
        long window = client.getWindow().handle();
        leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (leftDown && !wasLeftDown) {
            leftClicks.addLast(now);
        }
        if (rightDown && !wasRightDown) {
            rightClicks.addLast(now);
        }
        wasLeftDown = leftDown;
        wasRightDown = rightDown;
        prune(now);
    }

    private void prune(long now) {
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() >= 1000) {
            leftClicks.removeFirst();
        }
        while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() >= 1000) {
            rightClicks.removeFirst();
        }
    }

    public int leftCps() {
        prune(System.currentTimeMillis());
        return leftClicks.size();
    }

    public int rightCps() {
        prune(System.currentTimeMillis());
        return rightClicks.size();
    }

    public boolean leftDown() {
        return leftDown;
    }

    public boolean rightDown() {
        return rightDown;
    }
}