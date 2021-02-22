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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

import com.atlassian.jira.config.properties.APKeys
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.URIBuilder;






//Get mail server and SMTP server of jira to send messages
MailServerManager mailServerManager = ComponentAccessor.getMailServerManager()
SMTPMailServer mailServer = mailServerManager.getDefaultSMTPMailServer()

//Create lists for store values of all users and username
List<String> usersList = new ArrayList<String>();
List<String> AllUsersInfo = new ArrayList<String>();
List<String> Graph_Users = new ArrayList<String>();
List<String> Graph_Story_Points = new ArrayList<String>();


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
    String td_0 = '<td style="text-align:left">'+users+'</td>'

    //Define JQL query for advanced search in jira 
    def jql_Closed = 'status in ("Closed") and type not in ("Story") and resolved >= startOfMonth(-1) AND resolved <= endOfMonth(-1) and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    def jql_Done = 'status in ("Done") and type not in ("Story") and resolved >= startOfMonth(-1) AND resolved <= endOfMonth(-1) and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    def jql_In_Verification = 'status in ("In Verification") and type not in ("Story") and resolved >= startOfMonth(-1) AND resolved <= endOfMonth(-1) and assignee in ("'+usersList[x]+'") and "Story Points" is not empty'
    
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
     
     String td_4 = ""
     if(Total >= 0 && Total <= 20){
        String td_4_1 = "<td style='text-align:center ;background-color: #F30E0E'    > <a>"+Total+"</a>"+"</td>"
        td_4 += td_4_1
     }
     if(Total >= 21 && Total <= 49){
        String td_4_1 = "<td style='text-align:center ;background-color: #F3F30E'    > <a>"+Total+"</a>"+"</td>"
        td_4 += td_4_1
     }
     if(Total >= 50){
        String td_4_1 = "<td style='text-align:center ;background-color: #6B8E23'    > <a>"+Total+"</a>"+"</td>"
        td_4 += td_4_1
     }

    //Define string HTML content that have full row of each user and add it to AllUsersInfo list
    String txt = '<tr> '+ td_0 + td_1 + td_2 + td_3 + td_4 + '</tr>';
    AllUsersInfo.add(txt);
    
}
//Sort AllUsersInfo list descinding depended on greater value of story points 
for (int i = 0; i < AllUsersInfo.size(); i++) {
    for (int j = i; j < AllUsersInfo.size(); j++) {
        int number_i = Integer.parseInt(StringUtils.substringBetween(AllUsersInfo[i], "'    > <a>", "</a>"))
        int number_j = Integer.parseInt(StringUtils.substringBetween(AllUsersInfo[j], "'    > <a>", "</a>"))
        if (number_i < number_j) {
            def tempVar = AllUsersInfo[i];
            AllUsersInfo[i] = AllUsersInfo[j];
            AllUsersInfo[j] = tempVar;
        }
    }
}

//Convert all locations of AllUsersInfo list to one line string to make it easy for sending body
String ddd = ""
for (int y = 0; y < AllUsersInfo.size(); y++){
    ddd += AllUsersInfo[y]
}


for (int i = 0; i < AllUsersInfo.size(); i++) {
        String Full_name = StringUtils.substringBetween(AllUsersInfo[i], '<td style="text-align:left">', '</td>')
        String Total_Story_Points = StringUtils.substringBetween(AllUsersInfo[i], "'    > <a>", "</a>")
        Graph_Users.add('"'+Full_name+'"'+",")
        Graph_Story_Points.add(Total_Story_Points+",")
    }


String Graph_Users_String = ""
for (int y = 0; y < Graph_Users.size(); y++){
    Graph_Users_String += Graph_Users[y]
}


String Graph_Users_Story_String = ""
for (int y = 0; y < Graph_Story_Points.size(); y++){
    Graph_Users_Story_String += Graph_Story_Points[y]
}



String Base_URL = 'https://quickchart.io/chart?width=1500&height=1000&bkg=white&c='
String Final_Graph = '{"type":"horizontalBar","data":{"labels":['+Graph_Users_String+'],"datasets":[{"label":"Story Point EST","backgroundColor":getGradientFillHelper("horizontal",["#F30E0E","#F3F30E","#0EF310"]),"borderColor":getGradientFillHelper("horizontal",["#F30E0E","#F3F30E","#0EF310"]),"data":['+Graph_Users_Story_String+']},]},"options":{"plugins":{"datalabels":{"anchor":"end","align":"right","color":"#000000","font":{"size":"14","weight":"bold"},"borderColor":"#E74C3C","borderWidth":0,"borderRadius":5,"formatter":(value)=>{return value + "";},},},"elements":{"rectangle":{"borderWidth":2}},"responsive":true,"legend":{"position":"right"},"title":{"display":true,"text":"Monthly Story Points"}}}'
String encodedQuery = URLEncoder.encode(Final_Graph, StandardCharsets.UTF_8.toString());
String completeUrl = Base_URL + encodedQuery;



//Slack Jira_report to send chart:


// Once you have your webhook, Note the whole url for it in the variable below
final String webhookURL = "https://hooks.slack.com/services/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"
 
// Enter a channel name like #channel or a SlackUserId like @UH3DJXYZ7
final String channelOrUserId = "#jira-reports"
 
 
def client = new RESTClient("https://hooks.slack.com")
//def message = 'https://quickchart.io/chart?width=1500&height=1000&bkg=white&c=%7B%22type%22%3A%22horizontalBar%22%2C%22data%22%3A%7B%22labels%22%3A%5B%22Rami+Saad%22%2C%22Ivaylo+Dimov%22%2C%22Muhammad+Farhan+al-Saidi%22%2C%22Muhammad+Suhayl+Kanan%22%2C%22Sarmad+Hameed+Omran+Jasim+al-Ali%22%2C%22Nameer+Kamal+Matti+Bahnam+Hatturmi%22%2C%22Usamah+Ismail+Najm+Suhayl+al-Nuaymi%22%2C%22Hatim+Kareem%22%2C%22Mustafa+Muhammad%22%2C%22Ali+Faris%22%2C%22Muhaymin+Tariq+Shihab+Ahmad+al-Ani%22%2C%22Ali+Abid+Al-Hussain%22%2C%22Bariq+Husam%22%2C%22Muhammad+Ali+Hameed%22%2C%22Asil+Imad%22%2C%22Muhammad+Najeeb+Abid+al-Wahab+Hantush++al-Khazraji%22%2C%22Abbas+Yunis%22%2C%22Alaa+Imad+Mahdi+Salih+al-Saffar%22%2C%22Ali+Faiz%22%2C%22Ali+Abdul+al-Ghani+Abdul+Al-Hameed%22%2C%22Muhammad+Aziz+Muhammad+Ridha+Jabarah%22%2C%22Nuha+Talal%22%2C%22Muhsin+Anyat%22%2C%22Vyacheslav%22%2C%22Ihab+Abid+al-Wahab+Hantush+Muhammad+al-Khazraji%22%2C%22Jira+service%22%2C%22JIRA+loyal+bot%22%2C%22Labeeb+Abid+Allah+al-Ani%22%2C%22Abbas+Karrar%22%2C%22Ali+Hussain+Salman+Hussain+al-Khazraji%22%2C%22Mustafa+Mawlud+Hadeed+Ghanim%22%2C%22Ahmad+Riyad+Khudhayir+Hamad+al-Mashhadani%22%2C%22Muhammad+Abid+al-Qadir+Muhammad+Wali+al-Bayati%22%2C%22Murtadha+Saad+Kadhum+Muhsin+al-Sabagh%22%2C%22Abid+Allah+Hussain+al-Ubaydi%22%2C%22Ameer+turan%22%2C%22Alaa+Thamir+Abid+al-Khaliq+Jawad%22%2C%22Ban+Alaa%22%2C%22Mustafa+Muhammad+Hadi+Kadhum+al-Hamdani%22%2C%22Mohammad+Raad%22%2C%22Muhammad+Ali+Raid%22%2C%22Bashar+Methaq%22%2C%22Albert+Ghukasyan%22%2C%22Nadin+Muhannad%22%2C%22Haytham+Anmar%22%2C%22Omar+Mahir%22%2C%22Qusay+Raid+Abid+al-Qadir+Majeed+al-Khatib%22%2C%22administrator%22%2C%22Hasan+Muhammad+Hadi+Radi%22%2C%22Salah+Mahdi+Salih+Mahdi+al-Samarrai%22%2C%22Sarah+Ali+Jafar+Kareem+al-Ammari%22%2C%22Sarah+Juhayn+Jawdat+Faraj+al-Bayati%22%2C%22Al-Hussain+Salam+al-Sudani%22%2C%22Sura+Muthana+Mahdi+Fawzi+al-Jaylawi%22%2C%22Sayf+Salah+al-Din+Yasin++al-Nuaymi%22%2C%22Ahmad+Abid+al-Rahman%22%2C%22Yusuf+Nihad+al-Rawi%22%2C%22Yahya+Zakariya%22%2C%22Zahraa+Zuhayr%22%2C%5D%2C%22datasets%22%3A%5B%7B%22label%22%3A%22Story+Point+EST%22%2C%22backgroundColor%22%3AgetGradientFillHelper%28%22horizontal%22%2C%5B%22%23F30E0E%22%2C%22%23F3F30E%22%2C%22%230EF310%22%5D%29%2C%22borderColor%22%3AgetGradientFillHelper%28%22horizontal%22%2C%5B%22%23F30E0E%22%2C%22%23F3F30E%22%2C%22%230EF310%22%5D%29%2C%22data%22%3A%5B157%2C136%2C109%2C86%2C48%2C46%2C41%2C39%2C38%2C36%2C27%2C22%2C22%2C16%2C16%2C15%2C13%2C8%2C6%2C5%2C5%2C3%2C3%2C2%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C0%2C%5D%7D%2C%5D%7D%2C%22options%22%3A%7B%22plugins%22%3A%7B%22datalabels%22%3A%7B%22anchor%22%3A%22end%22%2C%22align%22%3A%22right%22%2C%22color%22%3A%22%23000000%22%2C%22font%22%3A%7B%22size%22%3A%2214%22%2C%22weight%22%3A%22bold%22%7D%2C%22borderColor%22%3A%22%23E74C3C%22%2C%22borderWidth%22%3A0%2C%22borderRadius%22%3A5%2C%22formatter%22%3A%28value%29%3D%3E%7Breturn+value+%2B+%22%22%3B%7D%2C%7D%2C%7D%2C%22elements%22%3A%7B%22rectangle%22%3A%7B%22borderWidth%22%3A2%7D%7D%2C%22responsive%22%3Atrue%2C%22legend%22%3A%7B%22position%22%3A%22right%22%7D%2C%22title%22%3A%7B%22display%22%3Atrue%2C%22text%22%3A%22Monthly+Story+Points%22%7D%7D%7D'

def data = [:]

data.put("channel", channelOrUserId)
//data.put("text", message)
data.put("iron_emoji", ":ghost:")
data.put("attachments",
    [
        [
            "fallback": "Report :",
            "color"   : "#6B8E23",
            "image_url" : completeUrl,

            "fields"  : [
                [
                    "title": "Report:",
                    "value": "Monthly Report Of Story Points For Last Month  :robot_face:",
                    "short": false
                ],
            ]
        ]
    ])

def response = client.post(
    path: new URIBuilder(webhookURL).path,
    contentType: ContentType.HTML,
    body: data,
    requestContentType: ContentType.JSON) as HttpResponseDecorator
 
assert response.status == 200 : "Request failed with status $response.status. $response.entity.content.text"








//Define CC and to whom message of table will send 
def bcc = "muhahameed@earthlink.iq,ntalal@earthlink.iq,baalaa@earthlink.iq,ariyad@earthlink.iq";
Email email = new Email("Ali_Alnakeeb<aalnakeeb@earthlink.iq>")
email.setMimeType("text/html")
email.setBcc(bcc)
email.setSubject("Monthly Story Points Report");
def text = '<!DOCTYPE html><html><head><style>table, th, td { border: 1px solid black;}</style></head><body><h2>Monthly Story Points Report</h2><p>Dears Ali Mahmoud, PM team. This is monthly report of all users in jira that calculate sum of Story Points for last month:</p><table style="width:75%" id="table_id"> <tr> <th rowspan="2">Full Name</th> <th colspan="4">Story Points</th> </tr> <tr> <th>Closed</th> <th>Done</th> <th>In Verification</th> <th style="text-align:center ;background-color: #E74C3C">Total</th> </tr>'+ddd+' </table></body></html>'
email.setBody(text)
mailServer.send(email)

