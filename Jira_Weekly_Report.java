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
    def jqlSearchStoryPoint = 'status in ("In Progress", "To Do", "In Verification") and assignee in ("'+usersList[x]+'") and "Story Points" is empty'
    def jql_search_story_points_last_10_days = 'updated > -7d and type not in ("Story") and assignee in ("'+usersList[x]+'") and "Story Points" is empty'
    def jql_sum_search_story_points_last_10_days = 'status in ("Done","Closed") and type not in ("Story") and updated > -7d and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    def jql_sum_search_story_points_last_10_days_not_completed = 'status in ("In Progress","In Verification","To Do") and type not in ("Story") and updated > -7d and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    def jqlSearchUpdated = 'status in ("In Progress", "To Do", "In Verification") and updated > -7d and assignee in ("'+usersList[x]+'")'
    def jqlSearch_Not_updated_2_Weeks = 'status in ("In Progress", "To Do", "In Verification") and updated < -14d and assignee in ("'+usersList[x]+'")'
    def jqlSearch = 'status in ("In Progress", "To Do", "In Verification") AND assignee in ("'+usersList[x]+'")'
    
    //Parse results of JQL search
    def query = jqlQueryParser.parseQuery(jqlSearch)
    def query_story_last_10_days = jqlQueryParser.parseQuery(jql_search_story_points_last_10_days)
    def query_sum_story_last_10_days = jqlQueryParser.parseQuery(jql_sum_search_story_points_last_10_days)
    def query_sum_story_last_10_days_not_completed = jqlQueryParser.parseQuery(jql_sum_search_story_points_last_10_days_not_completed)
    def queryupdated = jqlQueryParser.parseQuery(jqlSearchUpdated)
    def queryupdated_Not_updated_2_Weeks = jqlQueryParser.parseQuery(jqlSearch_Not_updated_2_Weeks)
    def queryOfStoryPoint = jqlQueryParser.parseQuery(jqlSearchStoryPoint)

    def results = searchService .search(user,query, PagerFilter.getUnlimitedFilter())
    def results_story_last_10_days = searchService .search(user,query_story_last_10_days, PagerFilter.getUnlimitedFilter())
    def results_sum_story_last_10_days = searchService .search(user,query_sum_story_last_10_days, PagerFilter.getUnlimitedFilter())
    def results_sum_story_last_10_days_not_completed = searchService .search(user,query_sum_story_last_10_days_not_completed, PagerFilter.getUnlimitedFilter())
    def resultsupdated = searchService .search(user,queryupdated, PagerFilter.getUnlimitedFilter())
    def results_Not_updated_2_Weeks = searchService .search(user,queryupdated_Not_updated_2_Weeks, PagerFilter.getUnlimitedFilter())
    def resultsOfStoryPoint = searchService .search(user,queryOfStoryPoint, PagerFilter.getUnlimitedFilter())
    
    //Get email address for each user 
    def email_of_user = resultsOfStoryPoint.results.collect{it.getAssignee().getEmailAddress()}
    

    //Convert results to string value
    def stringOfResults = String.valueOf(results.getResults())
    def stringOfResults_story_last_10_days = String.valueOf(results_story_last_10_days.getResults())
    def stringOfResultsUpdated = String.valueOf(resultsupdated.getResults())
    def stringOfResults_Not_updated_2_Weeks = String.valueOf(results_Not_updated_2_Weeks.getResults())
    def stringOfResultsOfStoryPoint = String.valueOf(resultsOfStoryPoint.getResults())

    //Check string of results if have no issues or have an issues of "has no updated" column 
    if (stringOfResultsUpdated != '[]'){
        UserData[3] = '<td style="text-align:center ;background-color: #E74C3C">No</td>'
    }

    if (stringOfResultsUpdated == '[]'){
        UserData[3] = '<td style="text-align:center ;background-color: #6B8E23">Yes</td>'
    }
    
    //Check string of results if have no issues or have an issues of "has no tasks" column 
    if (stringOfResults == '[]'){
        UserData[1] = '<td style="text-align:center ;background-color: #E74C3C">No</td>'
    }

    if (stringOfResults != '[]'){
        UserData[1] = '<td style="text-align:center ;background-color: #6B8E23">Yes</td>'
    }

    //Check string of results if have no issues or have an issues of "has no story points" column 
    if (stringOfResultsOfStoryPoint == '[]'){
        UserData[5] = '<td style="text-align:center ;background-color: #6B8E23">Yes</td>'
    }

    if (stringOfResultsOfStoryPoint != '[]'){
        UserData[5] = '<td style="text-align:center ;background-color: #E74C3C">No</td>'
    }

    //Check string of results if have no issues or have an issues of "have no updated task more than 2 weeks" column 
    if (stringOfResults_Not_updated_2_Weeks == '[]'){
        UserData[7] = '<td style="text-align:center ;background-color: #6B8E23">Yes</td>'
    }

    if (stringOfResults_Not_updated_2_Weeks != '[]'){
        UserData[7] = '<td style="text-align:center ;background-color: #E74C3C">No</td>'
    }

    //Fill values of UserData list by HTML content for each user via hyperlink of "Count" column (number of issues with it's hyperlink)
    def lengthoflistresults = results.getResults().size()
    UserData[2] = lengthoflistresults
    String td_2 = "<td style='text-align:center' > <a href='https://jira.earthlink.iq/issues/?jql="+jqlSearch+"'>"+UserData[2]+"</a>"+"</td>"
    def lengthoflistresultsupdated = resultsupdated.getResults().size()
    UserData[4] = lengthoflistresultsupdated
    String td_4 = "<td style='text-align:center'> <a href='https://jira.earthlink.iq/issues/?jql="+jqlSearchUpdated+"'>"+UserData[4]+"</a>"+"</td>"
    def lengthoflistnostorypoint = resultsOfStoryPoint.getResults().size()
    UserData[6] = lengthoflistnostorypoint
    String td_6 = "<td style='text-align:center'> <a href='https://jira.earthlink.iq/issues/?jql="+jqlSearchStoryPoint+"'>"+UserData[6]+"</a>"+"</td>"


    //Define string to fill it by string of email address of user
    def td_7_fixed_email = ''

    //Send message to each user if results of has not set story points has an issues (not null)
    if (stringOfResults_story_last_10_days != '[]'){
        
        //If results of email address of each user null, it will make email by username + @earthlink.iq
        if(email_of_user[0] == null){
            td_7_fixed_email += usersList[x]+'@earthlink.iq'

        }
        else{
            td_7_fixed_email += email_of_user[0]
        }

        //Define loop for each issue and convert it to HTML and hyperlink of users has not set story points of last 10 days
        def lengthoflistnostorypoints = results_story_last_10_days.getResults().size();
        def Issues_of_No_Story_Points = ''
        for(int xx = 0; xx < lengthoflistnostorypoints; xx++){
            def fixed = String.valueOf(results_story_last_10_days.getResults()[xx])
            def ff = fixed.substring(fixed.indexOf("=") + 1, fixed.indexOf("]"))
            Issues_of_No_Story_Points += '<tr><td style="text-align:center"> <a href="https://jira.earthlink.iq/browse/'+ ff +'">'+ff+'</a></td></tr>';
        }


        //Define string value to store HTML body
        String txt_story_points_users = '<!DOCTYPE html><html><head><style>table, th, td { border: 1px solid black;}</style></head><body><h2>Table Of Issues Has No Story Points</h2><p>Dear '+td_0+', You have to check below issues that have not set story points yet, you have to contact with your lead to set story points :</p><table style="width:50%" id="table_id"> <tr> <th>Issues</th></tr>'+Issues_of_No_Story_Points+' </table></body></html>'


        //This message will send for each user in jira of users that have no set story points to issues recently
        //Define email address of each user that will send message to him and set subject, body of message values 
        Email email = new Email('default<'+td_7_fixed_email+'>')
        email.setMimeType("text/html")
        email.setSubject("Issues Have No Story Points Report");
        def text = txt_story_points_users
        email.setBody(text)
        mailServer.send(email)
        
    }
    
    //Define loop to check issues of sum of story points that store on Sum variable 
    def length_of_sum_story_points = results_sum_story_last_10_days.getResults().size();
	Double Sum = 0;
    for(int xx = 0; xx < length_of_sum_story_points; xx++){
        def fixed = String.valueOf(results_sum_story_last_10_days.getResults()[xx])
        def ff = fixed.substring(fixed.indexOf("=") + 1, fixed.indexOf("]"))
        def customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName("Story Points")[0]
        def issue = ComponentAccessor.issueManager.getIssueByCurrentKey(ff) 
        def value = issue.getCustomFieldValue(customField)
		Sum += value;
    }

    //Create string variable to store HTML content of Sum 
    int Sum_Int = (int) Sum;
    String td_7 = "<td style='text-align:center' > <a>"+Sum_Int+"</a>"+"</td>"



    //Define loop to check issues of sum of story points that not completed yet and store on Sum_not_completed variable 
    def length_of_sum_story_points_not_completed = results_sum_story_last_10_days_not_completed.getResults().size();
	Double Sum_not_completed = 0;
    for(int xx = 0; xx < length_of_sum_story_points_not_completed; xx++){
        def fixed = String.valueOf(results_sum_story_last_10_days_not_completed.getResults()[xx])
        def ff = fixed.substring(fixed.indexOf("=") + 1, fixed.indexOf("]"))
        def customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectsByName("Story Points")[0]
        def issue = ComponentAccessor.issueManager.getIssueByCurrentKey(ff) 
        def value = issue.getCustomFieldValue(customField)
		Sum_not_completed += value;
    }

    //Create string variable to store HTML content of Sum_not_completed 
    int Sum_not_completed_Int = (int) Sum_not_completed;
    String td_8 = "<td style='text-align:center' > <a>"+Sum_not_completed_Int+"</a>"+"</td>"


    //Define string HTML content that have full row of each user and add it to AllUsersInfo list
    String txt = '<tr> '+ td_0 + UserData[1] + td_2 + UserData[3] + td_4 + UserData[5] + td_6 + UserData[7] + td_7 + td_8 +'</tr>';
    AllUsersInfo.add(txt);
    
    //Time sleep for sending messages for each user in Jira
    Thread.sleep(500);
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
email.setSubject("Users Report");
def text = '<!DOCTYPE html><html><head><style>table, th, td { border: 1px solid black;}</style></head><body><h2>Table Of Jira User Report</h2><p>Hello Ali Mahmoud, This is weekly report that you can find users have no task and its number of tasks if found, you can find whos not update thier tasks status more than 7 days ago, also you can find the who is havent story point and number of tasks</p><table style="width:100%" id="table_id"> <tr> <th rowspan="2">Full Name</th> <th rowspan="2">Open Tasks</th> <th rowspan="2">Count</th> <th rowspan="2">Updated Task</th> <th rowspan="2">Count</th> <th rowspan="2">Story Points Est</th> <th rowspan="2">Count</th> <th rowspan="2">2W Updated</th> <th colspan="2">Story Points</th> </tr> <tr> <th>Completed</th> <th>unCompleted</th> </tr>'+ddd+' </table></body></html>'
email.setBody(text)
mailServer.send(email)


