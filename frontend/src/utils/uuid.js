/**
 * Validates that a string is a standard 36-character UUID
 * (e.g. 3a8f1c7d-0000-1000-8000-000000000000)
 */
export const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export const isValidUUID = (value) => UUID_REGEX.test(value?.trim() ?? '');

/**
 * Validates a map of { fieldLabel: value } and returns the first error message,
 * or null if all are valid UUIDs.
 *
 * Example:
 *   const err = validateUUIDs({ 'Patient ID': form.patientId, 'Doctor ID': form.doctorId });
 *   if (err) { toast.error(err); return; }
 */
export const validateUUIDs = (fields) => {
  for (const [label, value] of Object.entries(fields)) {
    if (value && !isValidUUID(value)) {
      return `${label} must be a valid UUID (36-char format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx). Got: "${value}"`;
    }
  }
  return null;
};
