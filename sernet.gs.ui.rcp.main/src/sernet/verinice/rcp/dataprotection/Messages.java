package sernet.verinice.rcp.dataprotection;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.rcp.dataprotection.messages"; //$NON-NLS-1$
    public static String MigrateDataProtectionActionDelegate_error_dialog_message;
    public static String MigrateDataProtectionActionDelegate_error_dialog_titel;
    public static String MigrateDataProtectionActionDelegate_migration_log_plural;
    public static String MigrateDataProtectionActionDelegate_migration_log_singular;
    public static String MigrateDataProtectionActionDelegate_migration_finished_message;
    public static String MigrateDataProtectionActionDelegate_migration_finished_title;
    public static String MigrateDataProtectionActionDelegate_monitor_message;
    public static String MigrateDataProtectionActionDelegate_monitor_message_refresh;
    public static String MigrateDataProtectionDialog_message;
    public static String MigrateDataProtectionDialog_select_org;
    public static String MigrateDataProtectionDialog_title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
