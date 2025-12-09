package org.tasks.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuditEvent implements DataObject, Comparable<AuditEvent> {
    /** Event unique identifier */
    @Getter
    private Integer id;
    /** Event type */
    @NonNull
    @Getter
    private String type;
    /** Event initiator */
    @Getter
    private String author;
    /** Event start time */
    @NonNull
    private Instant startTime;
    /** Event end time */
    @Setter
    private Instant endTime;
    /** Event parameters */
    @NonNull
    @Getter
    private String parameters;
    /** Event result */
    @Getter
    @Setter
    private String result;

    public AuditEvent(@NonNull EventType type, String user, @NonNull Instant startTime, @NonNull String parameters) {
        this.type = type.name();
        this.author = user;
        this.startTime = startTime;
        this.parameters = parameters;
    }

    public String getStartTime() {
        return startTime.toString();
    }

    public String getEndTime() {
        return endTime != null ? endTime.toString() : null;
    }

    @Override
    public int compareTo(AuditEvent o) {
        int result = startTime.compareTo(o.startTime);
        if (result != 0) {
            return result;
        }

        if (endTime != null && o.endTime != null) {
            result = endTime.compareTo(o.endTime);
            if (result != 0) {
                return result;
            }
        }

        return id.compareTo(o.id);
    }
}
