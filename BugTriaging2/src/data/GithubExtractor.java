package data;
//import java.util.Collection;
//
//import org.eclipse.egit.github.core.IssueEvent;
//import org.eclipse.egit.github.core.client.GitHubClient;
//import org.eclipse.egit.github.core.client.GitHubRequest;
//import org.eclipse.egit.github.core.client.PageIterator;
//import org.eclipse.egit.github.core.service.IssueService;

public class GithubExtractor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//OAuth2 token authentication
//		system.out.println("aaaaa")
//		GitHubClient client = new GitHubClient();
//
//		client.setUserAgent("TaskAssignment/software-expertise");
//		client.setHeaderAccept("application/vnd.github.v3+json");
//		client.setOAuth2Token("19e383c976807df0359e36ba05938027e4a20c45");
//
//
////		client.setCredentials("alisajedi", "asmlz986");
////		client.setUserAgent("alisajedi/App123");
////		client.setHeaderAccept("application/vnd.github.v3+json");
////		client.setOAuth2Token("b2d87dc367ae6530bc7b7649e5ff48aa7931a19a");
//		System.out.println(client.getHeaderAccept());
//
//
//		IssueService is = new IssueService(client);
//		PageIterator<IssueEvent> pi = is.pageIssueEvents("rails", "rails", 26580);
////		while (pi.hasNext()){
//			Collection<IssueEvent> c = pi.next();
//			for (IssueEvent ii:c){
//				System.out.println("id: " + ii.getId() + "\n"
//						+ "label: " + ii.getLabel() + "\n"
//						+ "typeOfEvent: " + ii.getEvent() + "\n"
//						+ "date: " + ii.getCreatedAt().toGMTString() + "\n"
//						+ "actor: " + ii.getActor() + "\n"
//						+ "commitSHA: " + ii.getCommitId() + "\n"
//						+ "actor: " + ii.getActor() + "\n"
//						+ ii.getUrl() + "     " + ii.getCreatedAt().toGMTString()
//						);
//				break;
//			}
////		}
////		while (pi.hasNext()){
////			IssueEvent ie = pi.getRequest();
////		}
//
//		System.out.println(client.getRequestLimit());
//		System.out.println(client.getUser());


//		GitHubRequest gr = new GitHubRequest();
//		gr.setUri("https://api.github.com/repos/rails/rails/issues/1/events?per_page=100");

//		try {
//			System.out.println("--------------------------------------------");
//			RepositoryService service = new RepositoryService();
//			for (Repository repo : service.getRepositories("defunkt"))
//			  System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
//			System.out.println("--------------------------------------------");



//			GitHubResponse ghr = client.get(gr);
//			System.out.println("Body:");
//			System.out.println(ghr.getBody());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		
		
		
		
		
		
		
		
		
		
		
//		URL url = new URL("https://api.github.com/repos/rails/rails/issues/1/events?per_page=100");
//		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		
//		
//		conn.setRequestMethod("GET");
//		
//		conn.setRequestProperty("Accept", "application/json");
//
//		if (conn.getResponseCode() != 200) {
//			throw new RuntimeException("Failed : HTTP error code : "
//					+ conn.getResponseCode());
//		}
//
//		BufferedReader br = new BufferedReader(new InputStreamReader(
//			(conn.getInputStream())));
//
//		String output;
//		System.out.println("Output from Server .... \n");
//		while ((output = br.readLine()) != null) {
//			System.out.println(output);
//		}
//
//		conn.disconnect();

//	  } catch (MalformedURLException e) {
//
//		e.printStackTrace();
//
//	  } catch (IOException e) {
//
//		e.printStackTrace();
		

		
		
		
		
		
	}

}
