import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.util.JiraUtils
import com.atlassian.jira.workflow.WorkflowTransitionUtil
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder

def jiraAuth = ComponentAccessor.getJiraAuthenticationContext();
def currentUser = jiraAuth.getLoggedInUser();
def issueLinkManager = ComponentAccessor.getIssueLinkManager();
def linkCollection=issueLinkManager.getLinkCollection(issue,currentUser);
Collection<Issue> linkedIssues=linkCollection.getAllIssues();

CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

def fieldIds = [10202L,11707L,11706L,10713L] as Long[]

for(Issue tempIssue:linkedIssues){
    
    def pro_id=tempIssue.getProjectId();
    def issueTypeId=tempIssue.getIssueTypeId();
	def status=tempIssue.getStatusId();
    if(!status.equals("10106")||issueTypeId.equals("10101")){    //当前被链接Issue为任务类型或者状态为待发布时自动跳过
		continue;
	}
    //新需求   10103
    if(issueTypeId.equals("10103")||issueTypeId.equals("10100")){
            transfer(tempIssue,751);     
    }
    //缺陷   10104
    else if(issueTypeId.equals("10104")){
            transfer(tempIssue,791);
    }
	
	for(int i=0;i<fieldIds.length;i++){
		def customFiled=customFieldManager.getCustomFieldObject(fieldIds[i]);
		def customFiledValue=issue.getCustomFieldValue(customFiled);
		savevalue(tempIssue,customFiled,customFiledValue);
	}

}

public static void transfer(IssueImpl is,int actionId){
    WorkflowTransitionUtil workflowTransitionUtil = (WorkflowTransitionUtil)JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
    workflowTransitionUtil.setIssue(is);
    workflowTransitionUtil.setUserkey(is.getAssignee().name);
    workflowTransitionUtil.setAction(actionId);
    workflowTransitionUtil.validate();
    workflowTransitionUtil.progress();
}

public static void savevalue(IssueImpl is, CustomField cf, Object v)
{
    is.setCustomFieldValue(cf, v);
    Map<String, ModifiedValue> modifiedFields = is.getModifiedFields();
    FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().\
    getFieldLayout(is).getFieldLayoutItem(cf)
    DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
    final ModifiedValue modifiedValue = (ModifiedValue) modifiedFields.get(cf.getId());
    cf.updateValue(fieldLayoutItem, is, modifiedValue, issueChangeHolder);
}