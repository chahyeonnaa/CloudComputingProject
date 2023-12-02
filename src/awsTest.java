/*
 * Cloud Computing
 *
 * Dynamic Resource Management Tool
 * using AWS Java SDK Library
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;


import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;

import com.amazonaws.services.ec2.model.*;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

public class awsTest{

    static AmazonEC2 ec2;
    static Session session;

    private static void init() throws Exception {

        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")	/* check the region at AWS console */
                .build();
    }

    public static void main(String[] args) throws Exception {

        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        Scanner group = new Scanner(System.in);
        Scanner vpc = new Scanner(System.in);
        int number = 0;

        while(true)
        {
            System.out.println("                                                                ");
            System.out.println("                                                                ");
            System.out.println("----------------------------------------------------------------");
            System.out.println("           Amazon AWS Control Panel using SDK                   ");
            System.out.println("----------------------------------------------------------------");
            System.out.println("  1. list instance                2. available zones            ");
            System.out.println("  3. start instance               4. available regions          ");
            System.out.println("  5. stop instance                6. create instance from image ");
            System.out.println("  7. reboot instance              8. list images                ");
            System.out.println("  9. condor status                                              ");
            System.out.println("  10. list security group         11. create security group     ");
            System.out.println("                                  99. quit                      ");
            System.out.println("----------------------------------------------------------------");

            System.out.print("Enter an integer: ");

            if(menu.hasNextInt()){
                number = menu.nextInt();
            }else {
                System.out.println("concentration!");
                break;
            }


            String instance_id = "";
            String groupname = "";
            String vpc_id = "";

            switch(number) {
                case 1:
                    listInstances();
                    break;

                case 2:
                    availableZones();
                    break;

                case 3:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        startInstance(instance_id);
                    break;

                case 4:
                    availableRegions();
                    break;

                case 5:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        stopInstance(instance_id);
                    break;

                case 6:
                    System.out.print("Enter ami id: ");
                    String ami_id = "";
                    if(id_string.hasNext())
                        ami_id = id_string.nextLine();

                    if(!ami_id.isBlank())
                        createInstance(ami_id);
                    break;

                case 7:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        rebootInstance(instance_id);
                    break;

                case 8:
                    listImages();
                    break;

                case 9:
                    Condor_status();
                    break;

                case 10:
                    ListSecurity();
                    break;

                case 11:
                    System.out.print("Enter Security Group Name: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();
                        System.out.print("Enter description : ");
                        groupname = group.nextLine();
                    System.out.print("Enter vpc id : ");
                        vpc_id = vpc.nextLine();

                    if(!instance_id.isBlank())
                        createSecurity(instance_id, groupname, vpc_id);
                    break;

                case 99:
                    System.out.println("bye!");
                    menu.close();
                    id_string.close();
                    return;
                default: System.out.println("concentration!");
            }

        }

    }

    public static void createSecurity(String GroupName, String description, String vpc_id)
    {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        // 보안 그룹 생성 요청
        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
                .withGroupName(GroupName)
                .withDescription(description)
                .withVpcId(vpc_id);// VPC ID 입력

        CreateSecurityGroupResult response = ec2.createSecurityGroup(createSecurityGroupRequest);

        String securityGroupId = response.getGroupId();
        System.out.println("Created Security Group ID: " + securityGroupId);

        IpPermission sshPermission = new IpPermission()
                .withIpProtocol("tcp")
                .withFromPort(22)
                .withToPort(22);

        // AuthorizeSecurityGroupIngressRequest 객체 생성
        AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest()
                .withGroupId(securityGroupId)
                .withIpPermissions(sshPermission);

        ec2.authorizeSecurityGroupIngress(request);

        //ec2.authorizeSecurityGroupIngress(r -> r.setgroupId(securityGroupId).ipPermissions(sshPermission));


    }

    public static void ListSecurity()
    {
        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();

        DescribeSecurityGroupsResult response = ec2.describeSecurityGroups(request);

        for(SecurityGroup securityGroup : response.getSecurityGroups())
        {
            System.out.println("Security Group ID: " + securityGroup.getGroupId());
            System.out.println("Security Group Name: " + securityGroup.getGroupName());
            System.out.println("Description: " + securityGroup.getDescription());
            System.out.println("VPC ID: " + securityGroup.getVpcId());
            System.out.println("IP permissions: " + securityGroup.getIpPermissions());
            System.out.println("\n");
        }

    }
    public static void Condor_status()
    {
        String instanceIpAddress = "";
        connectInstance(instanceIpAddress);
        executeCS();

    }

    public static void executeCS()
    {
        try {
            ChannelExec channelExec = (ChannelExec)session.openChannel("exec");
            channelExec.setCommand("condor_status");
            channelExec.connect();


            BufferedReader reader = new BufferedReader(new InputStreamReader(channelExec.getInputStream(), StandardCharsets.UTF_8));
            System.out.println(reader.readLine());
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }


            while (!channelExec.isClosed()) {
                System.out.println("Command is still running...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            channelExec.disconnect();
            session.disconnect();

        } catch (IOException | JSchException e) {
            e.printStackTrace();
        }
    }
    public static void connectInstance(String Address)
    {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession("ec2-user", Address, 22);
            jsch.addIdentity("/");
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("Connected to the instance.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void listInstances() {

        System.out.println("Listing instances....");
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "[id] %s, " +
                                    "[AMI] %s, " +
                                    "[type] %s, " +
                                    "[state] %10s, " +
                                    "[monitoring state] %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
                System.out.println();
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }

    public static void availableZones()	{

        System.out.println("Available zones....");
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();

            AvailabilityZone zone;
            while(iterator.hasNext()) {
                zone = iterator.next();
                System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
            }
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    public static void startInstance(String instance_id)
    {

        System.out.printf("Starting .... %s\n", instance_id);
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
                () -> {
                    StartInstancesRequest request = new StartInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s", instance_id);
    }


    public static void availableRegions() {

        System.out.println("Available regions ....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeRegionsResult regions_response = ec2.describeRegions();

        for(Region region : regions_response.getRegions()) {
            System.out.printf(
                    "[region] %15s, " +
                            "[endpoint] %s\n",
                    region.getRegionName(),
                    region.getEndpoint());
        }
    }

    public static void stopInstance(String instance_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
                () -> {
                    StopInstancesRequest request = new StopInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        try {
            StopInstancesRequest request = new StopInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.stopInstances(request);
            System.out.printf("Successfully stop instance %s\n", instance_id);

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
        }

    }

    public static void createInstance(String ami_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(ami_id)
                .withInstanceType(InstanceType.T2Micro)
                .withMaxCount(1)
                .withMinCount(1);

        RunInstancesResult run_response = ec2.runInstances(run_request);

        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                reservation_id, ami_id);

    }

    public static void rebootInstance(String instance_id) {

        System.out.printf("Rebooting .... %s\n", instance_id);

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        try {
            RebootInstancesRequest request = new RebootInstancesRequest()
                    .withInstanceIds(instance_id);

            RebootInstancesResult response = ec2.rebootInstances(request);

            System.out.printf(
                    "Successfully rebooted instance %s", instance_id);

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
        }


    }

    public static void listImages() {
        System.out.println("Listing images....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeImagesRequest request = new DescribeImagesRequest();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

        request.getFilters().add(new Filter().withName("name").withValues("htcondor-slave-image"));
        request.setRequestCredentialsProvider(credentialsProvider);

        DescribeImagesResult results = ec2.describeImages(request);

        for(Image images :results.getImages()){
            System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                    images.getImageId(), images.getName(), images.getOwnerId());
        }

    }

}

	