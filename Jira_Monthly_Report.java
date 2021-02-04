import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.issue.search.SearchService 
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.issue.search.SearchQuery;
import org.apache.commons.lang3.StringUtils;
import com.atlassian.mail.Email
import com.atlassian.mail.server.MailServerManager
import com.atlassian.mail.server.SMTPMailServer
import com.atlassian.jira.component.ComponentAccessor;





//Get mail server and SMTP server of jira to send messages
MailServerManager mailServerManager = ComponentAccessor.getMailServerManager()
SMTPMailServer mailServer = mailServerManager.getDefaultSMTPMailServer()

//Create lists for store values of all users and username
List<String> usersList = new ArrayList<String>();
List<String> AllUsersInfo = new ArrayList<String>();

//Get active users of "development" group and add to usersList 
def groupManager = ComponentAccessor.getGroupManager()
groupManager.getUsersInGroup("development").findAll{user -> user.isActive()}.each { user ->
    usersList.add(user.username);
}

def arrayLength = usersList.size()


//Define and logged in users of jira 
def user = ComponentAccessor.jiraAuthenticationContext.loggedInUser
def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
def searchService = ComponentAccessor.getComponent(SearchService)


//Loop for each user in usersList 
for(int x = 0; x < arrayLength; x++){

    //int x = 54;

    //Get Fullname for each username
    def users = ComponentAccessor.getUserUtil().getUserObject(usersList[x]).getDisplayName()


    String[] UserData = new String[8];

    //Define HTML value of each row in column to fill it with the Fullname 
    String td_0 = '<td>'+users+'</td>'

    //Define JQL query for advanced search in jira 
    def jql_Closed = 'status in ("Closed") and type not in ("Story") and updated >= startOfMonth(-1) AND updated <= endOfMonth(-1) and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    def jql_Done = 'status in ("Done") and type not in ("Story") and updated >= startOfMonth(-1) AND updated <= endOfMonth(-1) and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    def jql_In_Verification = 'status in ("In Verification") and type not in ("Story") and updated >= startOfMonth(-1) AND updated <= endOfMonth(-1) and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    
    //Parse results of JQL search
    def query_Closed = jqlQueryParser.parseQuery(jql_Closed)
    def query_Done = jqlQueryParser.parseQuery(jql_Done)
    def query_In_Verification = jqlQueryParser.parseQuery(jql_In_Verification)

    def results_Closed = searchService .search(user,query_Closed, PagerFilter.getUnlimitedFilter())
    def results_Done = searchService .search(user,query_Done, PagerFilter.getUnlimitedFilter())
    def results_In_Verification = searchService .search(user,query_In_Verification, PagerFilter.getUnlimitedFilter())
   
    //Convert results to string value
    def stringOfResults_Closed = String.valueOf(results_Closed.getResults())
    def stringOfResults_Done = String.valueOf(results_Done.getResults())
    def stringOfResults_In_Verification = String.valueOf(results_In_Verification.getResults())


    //Define loop to check issues of sum of story points that store on Sum variable 
    def length_of_Closed = results_Closed.getResults().size();
	Double Sum_Closed = 0;
    for(int xx = 0; xx < length_of_Closed; xx++){
        def fixed = String.valueOf(results_Closed.getResults()[xx])
        def ff = fixed.substring(fixed.indexOf("=") + 1, fixed.indexOf("]"))
        def customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName("Story Points")[0]
        def issue = ComponentAccessor.issueManager.getIssueByCurrentKey(ff) 
        def value = issue.getCustomFieldValue(customField)
		Sum_Closed += value;
    }

    //Create string variable to store HTML content of Sum 
    int Sum_Int_Closed = (int) Sum_Closed;
    String td_1 = "<td style='text-align:center' > <a>"+Sum_Int_Closed+"</a>"+"</td>"


     //Define loop to check issues of sum of story points that store on Sum variable 
     def length_of_Done = results_Done.getResults().size();
     Double Sum_Done = 0;
     for(int xx = 0; xx < length_of_Done; xx++){
         def fixed = String.valueOf(results_Done.getResults()[xx])
         def ff = fixed.substring(fixed.indexOf("=") + 1, fixed.indexOf("]"))
         def customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName("Story Points")[0]
         def issue = ComponentAccessor.issueManager.getIssueByCurrentKey(ff) 
         def value = issue.getCustomFieldValue(customField)
         Sum_Done += value;
     }
 
     //Create string variable to store HTML content of Sum 
     int Sum_Int_Done = (int) Sum_Done;
     String td_2 = "<td style='text-align:center' > <a>"+Sum_Int_Done+"</a>"+"</td>"
 

     
     //Define loop to check issues of sum of story points that store on Sum variable 
     def length_of_In_Verification = results_In_Verification.getResults().size();
     Double Sum_In_Verification = 0;
     for(int xx = 0; xx < length_of_In_Verification; xx++){
         def fixed = String.valueOf(results_In_Verification.getResults()[xx])
         def ff = fixed.substring(fixed.indexOf("=") + 1, fixed.indexOf("]"))
         def customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName("Story Points")[0]
         def issue = ComponentAccessor.issueManager.getIssueByCurrentKey(ff) 
         def value = issue.getCustomFieldValue(customField)
         Sum_In_Verification += value;
     }
 
     //Create string variable to store HTML content of Sum 
     int Sum_Int_In_Verification = (int) Sum_In_Verification;
     String td_3 = "<td style='text-align:center' > <a>"+Sum_Int_In_Verification+"</a>"+"</td>"
 
     //Equation that calculate total of story points for each user
     int Total = Sum_Int_Closed + Sum_Int_Done + Sum_Int_In_Verification;
     String td_4 = "<td style='text-align:center ;background-color: #6B8E23' > <a>"+Total+"</a>"+"</td>"


    //Define string HTML content that have full row of each user and add it to AllUsersInfo list
    String txt = '<tr> '+ td_0 + td_1 + td_2 + td_3 + td_4 + '</tr>';
    AllUsersInfo.add(txt);
    
}

//Convert all locations of AllUsersInfo list to one line string to make it easy for sending body
String ddd = ""
for (int y = 0; y < AllUsersInfo.size(); y++){
    ddd += AllUsersInfo[y]
}

//Define CC and to whom message of table will send 
def bcc = "muhahameed@earthlink.iq,ntalal@earthlink.iq,baalaa@earthlink.iq,ariyad@earthlink.iq";
Email email = new Email("Ali_Alnakeeb<aalnakeeb@earthlink.iq>")
email.setMimeType("text/html")
email.setBcc(bcc)
email.setSubject("Monthly Story Points Report");
def text = '<!DOCTYPE html><html><head><style>table, th, td { border: 1px solid black;}</style></head><body><h2>Monthly Story Points Report</h2><p>Dears Ali Mahmoud, PM team. This is monthly report of all users in jira that calculate sum of Story Points for last month:</p><table style="width:75%" id="table_id"> <tr> <th rowspan="2">Full Name</th> <th colspan="4">Story Points</th> </tr> <tr> <th>Closed</th> <th>Done</th> <th>In Verification</th> <th style="text-align:center ;background-color: #E74C3C">Total</th> </tr>'+ddd+' </table></body></html>'
email.setBody(text)
mailServer.send(email)

