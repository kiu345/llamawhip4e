package com.github.kiu345.eclipse.eclipseai.ui.cc;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapterFactory;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;

public class InlinePromptProposal {

    private class PopupKeyAdapter extends KeyAdapter {
        private Shell popupShell;

        public PopupKeyAdapter(Shell popupShell) {
            this.popupShell = popupShell;
        }

        public void keyPressed(KeyEvent e) {
            switch (e.keyCode) {
                case SWT.CR:
                    e.doit = false;
                    if (state == 0 || (state != 0 && ((e.stateMask & SWT.CTRL) != 0))) {
                        try {
                            Display.getDefault().asyncExec(() -> {
                                label.setText("Prompt (...)");
                            });
                            state = 1;
                            String textContent = text.getText();
                            CompletableFuture.supplyAsync(() -> {
                                answer = ask(document, "java", selection.getOffset(), selection.getLength(), textContent);
                                return answer;

                            }).whenComplete((code, th) -> {
                                state = 2;
                                Display.getDefault().asyncExec(() -> {
                                    if (th != null) {
                                        log.error(th.getMessage(), th);
                                        if (!responseText.isDisposed()) {
                                            responseText.setText(th.getMessage());
                                        }
                                    }
                                    else {
                                        if (!responseText.isDisposed()) {
                                            responseText.setText("" + code);
                                        }
                                        if (!label.isDisposed()) {
                                            label.setText("Prompt (ENTER=Use (selected), CTRL+ENTER=Resend, ESC=Abort)");
                                        }
                                    }
                                });
                            });
                            break;
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (state == 2) {
                        try {
                            String selected = responseText.getSelectionText();
                            String target = (selected != null && selected.length() != 0 ? selected : responseText.getText());
                            document.replace(selection.getOffset(), selection.getLength(), "" + target);
                        }
                        catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                        popupShell.close();
                    }
                    break;
                case SWT.ESC:
                    if (state == 0) {
                        popupShell.close();
                        break;
                    }
                    if (state == 2) {
                        state = 0;
                        responseText.setText("");
                        text.setEnabled(true);
                    }
                    break;
            }
        }
    }

    private int state = 0;
    protected final PluginConfiguration config = PluginConfiguration.instance();

    private ILog log;
    private String answer;

    private Label label;
    private Text text;
    private Text responseText;

    private IDocument document;
    private ITextSelection selection;

    public InlinePromptProposal(ILog log) {
        this.log = log;
    }

    public void showInlinePrompt(IDocument document, ITextSelection selection, StyledText styledText) {
        this.document = document;
        this.selection = selection;

        Display display = Display.getDefault();
        Shell parentShell = display.getActiveShell();
        Point location = styledText != null
                ? styledText.toDisplay(styledText.getLocationAtOffset(styledText.getCaretOffset()))
                : display.getCursorLocation();

        Shell popupShell = new Shell(parentShell, SWT.NO_TRIM | SWT.ON_TOP | SWT.FILL | SWT.BORDER);
        popupShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        popupShell.setBounds(location.x, location.y + 20, 400, 200);

        GridLayout gridLayout = new GridLayout(1, false); // 1 Spalte
        gridLayout.marginTop = 1;
        gridLayout.marginBottom = 1;
        gridLayout.marginLeft = 1;
        gridLayout.marginRight = 1;
        gridLayout.verticalSpacing = 5;
        popupShell.setLayout(gridLayout);

        label = new Label(popupShell, SWT.SINGLE);
        label.setText("Prompt (ENTER=Send, ESC=Abort)");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        label.setBackground(display.getSystemColor(SWT.COLOR_TRANSPARENT));

        text = new Text(popupShell, SWT.SINGLE | SWT.BORDER);
        text.setMessage("Prompt...");
        text.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        responseText = new Text(popupShell, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        responseText.setEditable(false);
        responseText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        responseText.setBackground(display.getSystemColor(SWT.COLOR_TEXT_DISABLED_BACKGROUND));

        text.addKeyListener(new PopupKeyAdapter(popupShell));
        responseText.addKeyListener(new PopupKeyAdapter(popupShell));
//        var vl = new FocusListener() {
//
//            @Override
//            public void focusLost(FocusEvent e) {
//            }
//
//            @Override
//            public void focusGained(FocusEvent e) {
//                if (e.widget != text && e.widget != responseText) {
//                    popupShell.close();
//                }
//            }
//        };
//        text.addFocusListener(vl);
//        responseText.addFocusListener(vl);

        popupShell.open();
        text.setFocus();
    }

    protected String ask(IDocument doc, String filetype, int offset, int length, String userMesage) {
        var profile = config.getDefaultProfile();
        var modelName = config.getCcModel();
        if (profile == null || modelName == null) {
            return null;
        }
        String content = doc.get();
        String queryDoc = length == 0
                ? StringUtils.substring(content, 0, offset) + "${{CURSOR}}" + StringUtils.substring(content, offset)
                : StringUtils.substring(content, 0, offset)
                        + "${{CURSOR}}" +
                        StringUtils.substring(content, offset, offset + length) +
                        "${{ENDCURSOR}}"
                        + StringUtils.substring(content, offset + length);

        var adapter = ChatAdapterFactory.create(log, profile);
        var model = new ModelDescriptor(modelName, profile.getProvider().getInternalName());

        UserMsg request = new UserMsg("""
                <|CONTEXT|>
                %s
                </|CONTEXT|>
                ---
                %s

                """.formatted(queryDoc, userMesage));
        var response = adapter.generate(model, request);
        return response.getMessage();
    }
}
