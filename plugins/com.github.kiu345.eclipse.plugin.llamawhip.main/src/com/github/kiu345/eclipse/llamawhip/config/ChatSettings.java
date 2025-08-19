package com.github.kiu345.eclipse.llamawhip.config;

/**
 * Chat tab settings storage
 */
public class ChatSettings {
    private Boolean thinkingAllowed = true;
    private Boolean toolsAllowed = true;
    private Boolean webAllowed = false;
    private Integer temperatur = 10;
    private Integer repeatPenalty = 80;

    public ChatSettings() {
        super();
    }

    public ChatSettings(Boolean thinkingAllowed, Boolean toolsAllowed, Boolean webAllowed, Integer temperatur) {
        super();
        this.thinkingAllowed = thinkingAllowed;
        this.toolsAllowed = toolsAllowed;
        this.webAllowed = webAllowed;
        this.temperatur = temperatur;
    }

    public Boolean getThinkingAllowed() {
        return thinkingAllowed;
    }

    public void setThinkingAllowed(Boolean thinkingAllowed) {
        this.thinkingAllowed = thinkingAllowed;
    }

    public Boolean getToolsAllowed() {
        return toolsAllowed;
    }

    public void setToolsAllowed(Boolean toolsAllowed) {
        this.toolsAllowed = toolsAllowed;
    }

    public Boolean getWebAllowed() {
        return webAllowed;
    }

    public void setWebAllowed(Boolean webAllowed) {
        this.webAllowed = webAllowed;
    }

    public Integer getTemperatur() {
        return temperatur;
    }

    public void setTemperatur(Integer temperatur) {
        this.temperatur = cap(0, 200, temperatur);
    }

    public Integer getRepeatPenalty() {
        return repeatPenalty;
    }

    public void setRepeatPenalty(Integer repeatPenalty) {
        this.repeatPenalty = cap(0, 200, repeatPenalty);
    }

    private Integer cap(int min, int max, Integer input) {
        if (input == null) {
            return null;
        }
        if (input > max) {
            return max;
        }
        if (input < min) {
            return min;
        }
        return input;
    }
}
