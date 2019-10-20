package seedu.address.model.commonvariables;

import javax.sound.midi.MidiChannel;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Claim's claim amount in FinSec.
 */
public class Id {

    public static final String MESSAGE_CONSTRAINTS =
            "Id should only contain numbers";
    public static final String VALIDATION_REGEX = "\\d";
    private static Integer idCount = 0;
    public final String value;

    /**
     * Constructs an {@code Id}.
     *
     * @param id A valid claim Id.
     */
    public Id(String id) {
        requireNonNull(id);
        checkArgument(isValidId(id), MESSAGE_CONSTRAINTS);
        Integer idNum = Integer.parseInt(id);
        idCount = idCount < idNum
                ? idNum
                : idCount;
        value = id;
    }

    public static boolean isValidId(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    public static Id incrementId() {
        idCount++;
        return new Id(idCount.toString());
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this // short circuit if same object
                || (obj instanceof Id // instanceof handles nulls
                && value.equals(((Id) obj).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
