package com.vasten.cli.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudbilling.Cloudbilling;
import com.google.api.services.cloudbilling.model.BillingAccount;
import com.vasten.cli.entity.Clients;
import com.vasten.cli.entity.DeploymentStatus;
import com.vasten.cli.entity.Deployments;
import com.vasten.cli.entity.User;
import com.vasten.cli.error.ValidationError;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.repository.ClientsRepository;
import com.vasten.cli.repository.DeploymentsRepository;
import com.vasten.cli.repository.UserRepository;
import com.vasten.cli.service.DeploymentsService;
import com.vasten.cli.utility.ValidationUtility;

@Service
public class DeploymentsServiceImpl implements DeploymentsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsServiceImpl.class);

	@Value("${VARS_FILE_PATH}")
	public String varsFilePath;

	@Value("${FILE_PATH}")
	public String filePath;

	private static final String billingUrl = "https://cloudbilling.googleapis.com/v1/";

	@Autowired
	private DeploymentsRepository deploymentsRepository;

	@Autowired
	private ValidationUtility validationUtility;

	@Autowired
	private ClientsRepository clientsRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public Deployments createDeployment(int id, Deployments provisionData) {
		LOGGER.info("Creating deployment");

		validationUtility.validateDeploymentData(id, provisionData);

//		int clientId = provisionData.getClients().getId();

//		Deployments newDeployment = new Deployments();
//
//		Clients dbClient = clientsRepository.findOneById(id);
//
//		newDeployment.setClients(dbClient);
//		newDeployment.setName(provisionData.getName());
//		newDeployment.setStatus(DeploymentStatus.PENDING);
//
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
//		Calendar calobj = Calendar.getInstance();
//
//		String dateTime = df.format(calobj.getTime());
//
//		UUID uuid = UUID.randomUUID();
//
//		String prefix = id + "_" + uuid + "_" + dateTime;
//		LOGGER.info("prefix : " + prefix);
//
//		newDeployment.setPrefix(prefix);
//
//		try {
//			File file = new File("/home/scriptuit/varsfile/terraform.tfvars");
//			BufferedReader reader = new BufferedReader(new FileReader(file));
//			String line = "", oldtext = "";
//			while ((line = reader.readLine()) != null) {
//				oldtext += line + "\r\n";
//			}
//			reader.close();
//
//			String newtext = oldtext.replaceAll("vasten", provisionData.getName());
//
//			FileWriter writer = new FileWriter("/home/scriptuit/varsfile/terraform.tfvars");
//			writer.write(newtext);
//			writer.close();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//
//		return deploymentsRepository.save(newDeployment);

		User dbUser = userRepository.findOneById(id);

		Deployments newDeployment = new Deployments();

		newDeployment.setUser(dbUser);
		newDeployment.setName(provisionData.getName());
		newDeployment.setStatus(DeploymentStatus.PENDING);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
		Calendar calobj = Calendar.getInstance();

		String dateTime = df.format(calobj.getTime());

		UUID uuid = UUID.randomUUID();

		String prefix = id + "_" + uuid + "_" + dateTime;
		LOGGER.info("prefix : " + prefix);

		newDeployment.setPrefix(prefix);

		String fileName = provisionData.getName() + "_terraform.tfvars";
		newDeployment.setFileName(fileName);
		
		deploymentsRepository.save(newDeployment);

		FileInputStream instream = null;
		FileOutputStream outstream = null;
		File file = null;
		File outfile = null;

		try {
			file = new File(varsFilePath);
			outfile = new File(filePath + fileName);

			instream = new FileInputStream(file);
			outstream = new FileOutputStream(outfile);

			byte[] buffer = new byte[1024];

			int length;

			while ((length = instream.read(buffer)) > 0) {
				outstream.write(buffer, 0, length);
			}

			instream.close();
			outstream.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// String outputFile = new String(outstream.toString());
		try {
			BufferedReader reader = new BufferedReader(new FileReader(outfile));
			String line = "", oldtext = "";
			while ((line = reader.readLine()) != null) {
				oldtext += line + "\r\n";
			}
			reader.close();

			String newtext = oldtext.replaceAll("qwerty", provisionData.getName());

			FileWriter writer = new FileWriter(outfile);
			writer.write(newtext);
			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	

//		String newFilePath = outfile.getAbsolutePath();
		
		String[] cmd = { "/home/scriptuit/apply.sh" };

		ProcessBuilder pb = new ProcessBuilder(cmd);

		try {
			Process process = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String result = builder.toString();
			LOGGER.info(result);
			LOGGER.info("end of script execution");
		} catch (IOException e) {
			LOGGER.error("error");
			e.printStackTrace();
		}

		return newDeployment;
	}

	@Override
	public List<Deployments> getAll(int id, String name) {
		LOGGER.info("Getting all deployments");

		List<Deployments> deploymentList = new ArrayList<Deployments>();
		List<ValidationError> validationErrorList = new ArrayList<ValidationError>();

	//	validationUtility.validateClientId(clientId);

	//	Clients dbClient = clientsRepository.findOneById(clientId);
		
		User dbUser = userRepository.findOneById(id);

		if (name == null) {

			deploymentList = deploymentsRepository.findAllByUser(dbUser);
			return deploymentList;

		} else {

			Deployments dbDeployments = deploymentsRepository.findByName(name);

			if (dbDeployments == null) {
				LOGGER.error("Deployment with this name does not exist");
				validationErrorList.add(new ValidationError("name", "Deployment with this name does not exist"));

			} else {
				Deployments dbDeployment = deploymentsRepository.findByUserAndName(dbUser, name);
				deploymentList.add(dbDeployment);
				return deploymentList;
			}

			if (validationErrorList != null && !validationErrorList.isEmpty()) {
				throw new CliBadRequestException("Bad Request", validationErrorList);
			}
		}
		return deploymentList;

	}

	@Override
	public Deployments getStatus(String name) {
		LOGGER.info("Getting status");

		validationUtility.validateDeploymentName(name);

		Deployments dbDeployment = deploymentsRepository.findByName(name);

		return dbDeployment;
	}

	@Override
	public float getCost(String name) {
		LOGGER.info("Getting cost of deployment");

		validationUtility.validateDeploymentName(name);

//		Deployments dbDeployment = deploymentsRepository.findByName(name);
//		Clients dbClient = clientsRepository.findOneById(dbDeployment.getClients().getId());
//		String email = dbClient.getEmail();
//
//		try {
//			float cost = getDploymentCost(name, billingUrl);
//		} catch (IOException | GeneralSecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return 0;
	}

	private float getDploymentCost(String name, String billingUrl) throws IOException, GeneralSecurityException {
//		LOGGER.info("Getting cost of client's deployment");
//
////		String newUrl = billingUrl + name;
////		
////		RestTemplate template = new RestTemplate();
////
////		HttpHeaders headers = new HttpHeaders();
////		headers.set("Content-Type", "application/json");
////
////		HttpEntity<Object> entity = new HttpEntity<>(null, headers);
////
////		ResponseEntity<BillingAccount> response = template.exchange(newUrl, HttpMethod.GET, entity,
////				BillingAccount.class);
//
//		// Authentication is provided by the 'gcloud' tool when running locally
//		// and by built-in service accounts when running on GAE, GCE, or GKE.
//		GoogleCredential credential = GoogleCredential.getApplicationDefault();
//
//		// The createScopedRequired method returns true when running on GAE or a local
//		// developer
//		// machine. In that case, the desired scopes must be passed in manually. When
//		// the code is
//		// running in GCE, GKE or a Managed VM, the scopes are pulled from the GCE
//		// metadata server.
//		// See
//		// https://developers.google.com/identity/protocols/application-default-credentials
//		// for more information.
//		if (credential.createScopedRequired()) {
//			credential = credential
//					.createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
//		}
//
//		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//		Cloudbilling cloudbillingService = new Cloudbilling.Builder(httpTransport, jsonFactory, credential)
//				.setApplicationName("Google Cloud Platform Sample").build();
//
//		// TODO: Change placeholders below to appropriate parameter values for the 'get'
//		// method:
//		// The resource name of the billing account to retrieve. For example,
//		// `billingAccounts/012345-567890-ABCDEF`.
//		String name1 = "billingAccounts/name";
//
//		Cloudbilling.BillingAccounts.Get request = cloudbillingService.billingAccounts().get(name1);
//		BillingAccount response = request.execute();
//
//		LOGGER.info("response : " + response);

		return 0;
	}

}
