package sernet.verinice.service.commands.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.iso27k.ControlMaturityService;

@SuppressWarnings("serial")
/**
 * Returns two values: total topics and answered topics (state != default
 * value).
 */
public class SamtProgressSummary extends GenericCommand {

    public static final String ANSWERED = Messages.SamtProgressSummary_0;

    public static final String UNANSWERED = Messages.SamtProgressSummary_1;

    private Integer id;

    private Map<String, Integer> result;

    public SamtProgressSummary(ControlGroup cg) {
        id = cg.getDbId();
    }

    @Override
    public void execute() {
        result = new HashMap<>(2);
        result.put(UNANSWERED, 0);
        result.put(ANSWERED, 0);

        IBaseDao<ControlGroup, Serializable> dao = getDaoFactory().getDAO(ControlGroup.class);
        ControlGroup cg = dao.findById(id);
        loadSamtTopics(cg);
    }

    private void loadSamtTopics(ControlGroup cg) {
        if (cg == null) {
            return;
        }
        ControlMaturityService maturityService = new ControlMaturityService();

        for (CnATreeElement e : cg.getChildren()) {
            if (e instanceof SamtTopic) {
                SamtTopic st = (SamtTopic) e;
                // ignore chapters 0.x (Copyright et al):
                if (!st.getTitle().startsWith("0")) { //$NON-NLS-1$
                    if (IControl.IMPLEMENTED_NOTEDITED.equals(maturityService.getIsaState(st))) {
                        result.put(UNANSWERED, (Integer) result.get(UNANSWERED) + 1);
                    } else {
                        result.put(ANSWERED, (Integer) result.get(ANSWERED) + 1);
                    }
                }
            } else if (e instanceof ControlGroup) {
                loadSamtTopics((ControlGroup) e);
            }
        }
    }

    public Map<String, Integer> getResult() {
        return result;
    }
}