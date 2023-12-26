package mysql.modules.submission;

import constants.SubmissionType;
import core.CustomObservableMap;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;

public class SubmissionData extends Observable {

    private final CustomObservableMap<Long, SubmissionSlot> submissions;

    public SubmissionData(Map<Long, SubmissionSlot> submissions) {
        this.submissions = new CustomObservableMap<>(submissions);
    }

    public CustomObservableMap<Long, SubmissionSlot> getSubmissions() {
        return submissions;
    }

    public Optional<SubmissionSlot> getSubmission(long userId, SubmissionType submissionType) {
        return submissions.values().stream().filter(submissionSlot -> submissionSlot.getUserId() == userId && submissionSlot.getSubmissionType() == submissionType).findFirst();
    }

}
