package de.cognicrypt.core.telemetry;

/**
 * Possible telemetry events.
 *
 */
public enum TelemetryEvents {
	START, STOP, POST_BUILD, ANALYSIS_INTERNAL_ERROR, ANALYSIS_FINISHED, ANALYSIS_ABORTED, ANALYSIS_ERROR_SETUP, SOOT_EXCEPTION, WIZARD_FILE_SELECTED_GENERATION, WIZARD_RETURNED, WIZARD_ANSWER_SELECTED, WIZARD_TASK_SELECTED, WIZARD_OPENED, WIZARD_NEW_TASK_SELECTED
}