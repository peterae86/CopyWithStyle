package com.peterae86;

import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

/**
 * Created by xiaorui.guo on 2016/6/3.
 */
public class PopupChoiceAction extends DumbAwareAction {
    private ActionGroup actionGroup;


    public PopupChoiceAction() {
        actionGroup = (ActionGroup) ActionManager.getInstance().getAction("MyExportGroup");
    }

    public void actionPerformed(AnActionEvent e) {
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(null, actionGroup,
                e.getDataContext(), JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, false);

        popup.showInBestPositionFor(e.getDataContext());
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Project project = getEventProject(e);
        if (project != null) {
            e.getPresentation().setEnabled(LookupManager.getInstance(project).getActiveLookup() == null);
        }
    }
}
