package com.github.kiu345.eclipse.eclipseai.ui.util;

import java.util.function.IntConsumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

/**
 * Listener for a {@link Combo} that forwards the selected index to an {@link IntConsumer}.
 */
public class ComboBoxIdSelectionListener implements SelectionListener {
    private IntConsumer consumer;

    public ComboBoxIdSelectionListener(IntConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.widget instanceof Combo combo) {
            processCombo(combo);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        if (e.widget instanceof Combo combo) {
            processCombo(combo);
        }
    }

    private void processCombo(Combo combo) {
        consumer.accept(combo.getSelectionIndex());
    }

}
