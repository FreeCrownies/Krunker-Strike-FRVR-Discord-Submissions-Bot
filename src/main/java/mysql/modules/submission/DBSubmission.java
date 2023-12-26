package mysql.modules.submission;

import constants.SubmissionType;
import core.cache.SingleCache;
import mysql.DBDataLoad;
import mysql.DBMain;

import java.util.Map;

public class DBSubmission extends SingleCache<SubmissionData> {

    private static final DBSubmission ourInstance = new DBSubmission();

    private DBSubmission() {
    }

    public static DBSubmission getInstance() {
        return ourInstance;
    }

    @Override
    protected SubmissionData fetchValue() {
        SubmissionData submissionData = new SubmissionData(getSubmissions());
        submissionData.getSubmissions()
                .addMapAddListener(this::addSubmissionSlot)
                .addMapRemoveListener(this::removeSubmissionSlot);
        return submissionData;
    }

    private Map<Long, SubmissionSlot> getSubmissions() {
        return new DBDataLoad<SubmissionSlot>("Submissions", "user_id, message_id, message_id_video, type, media_url, description")
                .getMap(
                        SubmissionSlot::getMessageId,
                        resultSet -> new SubmissionSlot(
                                resultSet.getLong(1),
                                resultSet.getLong(2),
                                resultSet.getObject(3) == null ? null : resultSet.getLong(3),
                                SubmissionType.values()[resultSet.getInt(4)],
                                resultSet.getString(5),
                                resultSet.getString(6)
                        )
                );
    }

    private void addSubmissionSlot(SubmissionSlot submissionSlot) {
        DBMain.getInstance().asyncUpdate("REPLACE INTO Submissions VALUES (?, ?, ?, ?, ?, ?)", preparedStatement -> {
                    preparedStatement.setLong(1, submissionSlot.getUserId());
                    preparedStatement.setLong(2, submissionSlot.getMessageId());
                    preparedStatement.setObject(3, submissionSlot.getMessageIdVideo());
                    preparedStatement.setInt(4, submissionSlot.getSubmissionType().ordinal());
                    preparedStatement.setString(5, submissionSlot.getMediaUrl());
                    preparedStatement.setString(6, submissionSlot.getDescription());
                }
        );
    }

    private void removeSubmissionSlot(SubmissionSlot submissionSlot) {
        DBMain.getInstance().asyncUpdate("DELETE FROM Submissions WHERE message_id = ?", preparedStatement -> preparedStatement.setLong(1, submissionSlot.getMessageId()));
    }

}
