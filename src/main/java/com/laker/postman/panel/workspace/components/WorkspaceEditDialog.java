package com.laker.postman.panel.workspace.components;

import com.laker.postman.model.Workspace;
import com.laker.postman.service.EnvironmentService;
import com.laker.postman.service.WorkspaceService;
import com.laker.postman.util.FontsUtil;
import com.laker.postman.util.I18nUtil;
import com.laker.postman.util.MessageKeys;
import com.laker.postman.util.SystemUtil;
import com.laker.postman.util.WorkspaceStorageUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

@Slf4j
public class WorkspaceEditDialog extends ProgressDialog {
    @Getter
    private transient Workspace workspace;

    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField environmentUrlField;

    private String environmentBaseUrl;
    private String updatedName;
    private String updatedDescription;

    public WorkspaceEditDialog(Window parent, Workspace workspace) {
        super(parent, I18nUtil.getMessage(MessageKeys.WORKSPACE_EDIT));
        this.workspace = workspace;
        initComponents();
        initDialog();
    }

    private void initComponents() {
        nameField = new JTextField(10);
        descriptionArea = new JTextArea(3, 10);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        environmentUrlField = new JTextField(15);

        nameField.setText(workspace.getName());
        descriptionArea.setText(workspace.getDescription() == null ? "" : workspace.getDescription());
        environmentUrlField.setText(EnvironmentService.getWorkspaceBaseUrl(SystemUtil.getEnvPathForWorkspace(workspace)));

        Font defaultFont = FontsUtil.getDefaultFont(Font.PLAIN);
        nameField.setFont(defaultFont);
        descriptionArea.setFont(defaultFont);
        environmentUrlField.setFont(defaultFont);

        if (WorkspaceStorageUtil.isDefaultWorkspace(workspace)) {
            nameField.setEnabled(false);
        }

        progressPanel = new ProgressPanel(I18nUtil.getMessage(MessageKeys.WORKSPACE_INFO));
        progressPanel.setVisible(false);
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                I18nUtil.getMessage(MessageKeys.WORKSPACE_INFO),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FontsUtil.getDefaultFont(Font.BOLD)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel(I18nUtil.getMessage(MessageKeys.WORKSPACE_NAME) + ":"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPanel.add(new JLabel(I18nUtil.getMessage(MessageKeys.WORKSPACE_DESCRIPTION) + ":"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        contentPanel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPanel.add(new JLabel(I18nUtil.getMessage(MessageKeys.WORKSPACE_ENVIRONMENT_URL) + ":"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(environmentUrlField, gbc);

        containerPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        bottomPanel.add(progressPanel, BorderLayout.NORTH);
        bottomPanel.add(createStandardButtonPanel(I18nUtil.getMessage(MessageKeys.WORKSPACE_EDIT)), BorderLayout.SOUTH);

        containerPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(containerPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(520, 420));
    }

    @Override
    protected void setupEventHandlers() {
        // no-op
    }

    @Override
    protected void validateInput() throws IllegalArgumentException {
        updatedName = nameField.getText().trim();
        updatedDescription = descriptionArea.getText().trim();
        environmentBaseUrl = environmentUrlField.getText().trim();

        if (nameField.isEnabled() && updatedName.isEmpty()) {
            throw new IllegalArgumentException(I18nUtil.getMessage(MessageKeys.WORKSPACE_VALIDATION_NAME_REQUIRED));
        }
    }

    @Override
    protected SwingWorker<Void, String> createWorkerTask() {
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                setProgress(10);
                try {
                    WorkspaceService.getInstance().updateWorkspaceBasicInfo(
                            workspace.getId(),
                            updatedName,
                            updatedDescription
                    );
                    EnvironmentService.updateWorkspaceBaseUrl(SystemUtil.getEnvPathForWorkspace(workspace), environmentBaseUrl);
                    setProgress(100);
                } catch (Exception e) {
                    log.error("Failed to update workspace", e);
                    throw e;
                }
                return null;
            }
        };
    }

    @Override
    protected void setInputComponentsEnabled(boolean enabled) {
        if (!WorkspaceStorageUtil.isDefaultWorkspace(workspace)) {
            nameField.setEnabled(enabled);
        }
        descriptionArea.setEnabled(enabled);
        environmentUrlField.setEnabled(enabled);
    }
}
