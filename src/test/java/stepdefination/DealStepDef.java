package stepdefination;

import Util.Utility;
import com.agilecrm.types.CustomDataDto;
import com.agilecrm.types.DealDto;
import com.agilecrm.types.DealResponseDto;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.*;

public class DealStepDef {
    RequestSpecification requestSpecification;
    Response response;

    Map<String, Object> dealBody;
    DealDto dealDto;

    Utility utility;


    public void createDealStructure(DataTable table) {
        Map<String, String> data = table.asMaps().get(0); //data table extracted in the form of Map
        //get comma separated contact ids and split them then convert into integers and then add into array
        List<Integer> contactIdList = new ArrayList<>();
        if (Objects.nonNull(data.get("contactIds"))) {
            String[] contactIds = data.get("contactIds").split(",");
            for (String id : contactIds) {
                int contactId = Integer.parseInt(id);
                contactIdList.add(contactId);
            }

        }

        //prepare custom Data object
        List<Map<String, String>> customData = new ArrayList<>();
        Map<String, String> customObj = new HashMap<>();
        if (Objects.nonNull(data.get("customData"))) {
            String[] dataObject = data.get("customData").split(",");
            customObj.put("name", dataObject[0]);
            customObj.put("value", dataObject[1]);
            customData.add(customObj);
        }


        //get the name from feature file
        //if value is not null then get the value as it is else consider it as null
        Object name = Objects.nonNull(data.get("name")) ? data.get("name") : null;  //ternary operator..if else

        //get expectedValue from dataTable/feature file
        //if the value is not null then get the value as it is else consider it as null
        //else take the value as it is
        Object expectedValue = null;
        if (Objects.nonNull(data.get("expectedValue"))) {
            try {
                expectedValue = Float.valueOf(data.get("expectedValue"));
            } catch (Exception e) {
                expectedValue = data.get("expectedValue");
            }
        }


        //get the probability from dataTable/feature file
        //if value is not null or valid string
        //then convert into integer else take as it is
        Object probability = null;
        if (Objects.nonNull(data.get("probability"))) {
            try {
                probability = Integer.parseInt(data.get("probability"));
            } catch (Exception e) {
                probability = data.get("probability");
            }
        }

        Object milestone = Objects.nonNull(data.get("milestone")) ? data.get("milestone") : null;

        dealBody = new HashMap<>();
        dealBody.put("name", name);
        dealBody.put("expected_Value", expectedValue);
        dealBody.put("probability", probability);
        dealBody.put("milestone", milestone);
        dealBody.put("contact_ids", contactIdList);
        dealBody.put("custom_data", customData);


    }

    @Given("I prepare request structure to get deal")
    public void iPrepareRequestStructureToGetDeal(Map<String, String> table) {
        requestSpecification = RestAssured.given();
        String headers = table.get("header");
        String headerKey = headers.split(":")[0];
        String headerValue = headers.split(":")[1];
        requestSpecification.baseUri("https://webapitesting.agilecrm.com")
                .basePath("/dev/api/")
                .header("Accept", ContentType.JSON)
                .header("Content-Type", ContentType.JSON)
                //.auth().basic(table.get("username"),table.get("password")).log().all();
                .auth().basic("poojashah@yopmail.com", "rl8kb61ufee7iaktvp06i1ehk7")
                .body(dealBody).log().all();
    }




    @When("I hit a get deal API")
    public void iHitAGetDealAPI(Map<String, String> table) {
        String endpoint;
        if (table.get("pathParam") != null && !table.get("pathParam").equals("null")) {
            endpoint = table.get("endpoint") + "/" + table.get("pathParam");
        } else {
            endpoint = table.get("endpoint");
        }
        System.out.println("path: " + endpoint);
        response = requestSpecification.get(endpoint);
        response.prettyPrint();

    }

    @Then("I verify the deal information using {string} and status code should be {int}")
    public void iVerifyTheDealInformationUsingAndStatusCodeShouldBeStatusCode(String expectedId, int statusCode, Map<String, String> table) {
        int actualStatusCode = response.statusCode();
        Assert.assertEquals(statusCode, actualStatusCode);
        boolean valid = Boolean.parseBoolean(table.get("valid")); //String "true","false" converted to boolean valid should be true or false hence we convert valid into boolean
        if (valid) //by default here valid means true
        {
            String actualId = response.jsonPath().getString("id");
            Assert.assertEquals(expectedId, actualId);

        } else {
            System.out.println("API response has no content");
        }
    }



    @Given("I prepare request structure to create deal")
    public void iPrepareRequestStructureToCreateDeal(DataTable dataTable) {

        {
            createDealStructure(dataTable);
//       Map<String,String> data=dataTable.asMaps().get(0);
//       List<Map<String,String>> customData=new ArrayList<>();
//       Map<String,String> customDataValue=new HashMap<>();
//       customDataValue.put("name","Group Size");
//       customDataValue.put("value","10");
//       customData.add(customDataValue);
//       List<Long> contactIds=new ArrayList<>();
            requestSpecification = RestAssured.given();
            requestSpecification.baseUri("https://webapitesting.agilecrm.com")
                    .basePath("/dev/api/")
                    .header("Accept", ContentType.JSON)
                    .header("Content-Type", ContentType.JSON)
                    //.auth().basic(table.get("username"),table.get("password")).log().all();
                    .auth().basic("poojashah@yopmail.com", "rl8kb61ufee7iaktvp06i1ehk7")
                    .body(dealBody)
                    .log().all();
        }
    }

    @When("I hit an api to create a deal")
    public void iHitAnApiToCreateADeal()
    {
        response = requestSpecification.post("/opportunity");
    }

    @Then("I verify the deal created successfully using statusCode {string}")
    public void iVerifyTheDealCreatedSuccessfullyUsingStatusCodeStatusCode(String statusCode,DataTable dataTable) //we have mentioned statuscode as a string hence converted it into int
    {
        Assert.assertEquals(Integer.parseInt(statusCode), response.statusCode());
        response.prettyPrint();
        if (response.statusCode() == 200) {
            Assert.assertEquals(dealBody.get("name"), response.jsonPath().get("name"));
            Assert.assertEquals(dealBody.get("expected_Value"), response.jsonPath().get("expectedValue"));
            Assert.assertEquals(dealBody.get("probability"), response.jsonPath().get("probability"));
            Assert.assertEquals(dealBody.get("milestone"), response.jsonPath().get("milestone"));
            Assert.assertEquals(dealBody.get("contact_ids"), response.jsonPath().getList("contactIds"));
            Assert.assertEquals(dealBody.get("custom_data"), response.jsonPath().getList("customData"));

        }
    }


    @Given("I prepare request structure to create deal using serialization concept")
    public void iPrepareRequestStructureToCreateDealUsingSerializationConcept(DataTable table) {
        utility=new Utility();
        dealDto=new DealDto();

        Map<String,String> dataTable=table.asMaps().get(0);
        dealDto.setName(dataTable.get("name"));
        dealDto.setExpected_value(Float.parseFloat(dataTable.get("expectedValue")));
        dealDto.setProbability(Integer.parseInt(dataTable.get("probability")));
        dealDto.setMilestone(dataTable.get("milestone"));
        dealDto.setClose_date(45354765);

        List<Long> contactIds=utility.setContactListForDeal(dataTable);
        dealDto.setContact_ids(contactIds);

        List<CustomDataDto> custom_data=utility.setCustomDataForDeal(dataTable);
        dealDto.setCustom_data(custom_data);


        requestSpecification=RestAssured.given();
        requestSpecification.baseUri("https://webapitesting.agilecrm.com")
                .basePath("/dev/api")
                .header("Accept",ContentType.JSON)
                .header("Content-Type",ContentType.JSON)
                .body(dealDto)
                .auth().basic("poojashah@yopmail.com","rl8kb61ufee7iaktvp06i1ehk7")
                .log().all();
        response=requestSpecification.post("/opportunity");
    }

    @Then("I verify the deal created successfully using {string}")
    public void iVerifyTheDealCreatedSuccessfullyUsing(String statusCode,DataTable table) {
        response.prettyPrint();
        if(response.statusCode()==200)
        {
            Assert.assertEquals(dealDto.getName(),response.jsonPath().get("name"));
            Assert.assertEquals(dealDto.getExpected_value(),response.jsonPath().get("expected_value"));
            Assert.assertEquals(dealDto.getProbability(),response.jsonPath().get("probability"));
            Assert.assertEquals(dealDto.getMilestone(),response.jsonPath().get("milestone"));
            Assert.assertEquals(dealDto.getContact_ids(),response.jsonPath().getList("contact_ids"));
            Assert.assertEquals(dealDto.getCustom_data(),response.jsonPath().getList("custom_data"));

        }


    }


    @Given("I prepare request structure to create deal using serialization and Deserialization concept")
    public void iPrepareRequestStructureToCreateDealUsingSerializationDeserializationConcept(DataTable table) {
        dealDto = new DealDto();
        utility=new Utility();
         Map<String,String> dataTable=table.asMaps().get(0);
        dealDto.setName(dataTable.get("name"));
        dealDto.setExpected_value(Float.parseFloat(dataTable.get("expectedValue")));
        dealDto.setProbability(Integer.parseInt(dataTable.get("probability")));
        dealDto.setMilestone(dataTable.get("milestone"));
        dealDto.setClose_date(2426272);

        List<Long> contactListForDeal=utility.setContactListForDeal(dataTable);
        dealDto.setContact_ids(contactListForDeal);

        List<CustomDataDto> customDataForDeal=utility.setCustomDataForDeal(dataTable);
        dealDto.setCustom_data(customDataForDeal);

        requestSpecification=RestAssured.given();
        requestSpecification.baseUri("https://webapitesting.agilecrm.com")
                .basePath("/dev/api/")
                .header("Content-Type", ContentType.JSON)
                .body(dealDto)
                .auth().basic("poojashah@yopmail.com","rl8kb61ufee7iaktvp06i1ehk7")
                .log().all();

         response = requestSpecification.post("/opportunity");
    }


    @Then("I verify the api using deserialization concept")
    public void iVerifyTheApiUsingDeserializationConcept()
    { response.prettyPrint();
        //Deserialization syntax=classname refvar=response.as(classname.class)
        DealResponseDto dealResponseDto=response.as(DealResponseDto.class);
        System.out.println(dealResponseDto.getId());
        Assert.assertEquals(dealDto.getName(),dealResponseDto.getName());
        Assert.assertTrue(Objects.nonNull(dealResponseDto.getId()));

        Optional.of(dealDto.getExpected_value()).ifPresent(val->{
                 Float expectValue=Float.parseFloat(String.valueOf(val));
        Float actualValue =Float.parseFloat(String.valueOf(dealResponseDto.getExpected_value()));
        Assert.assertEquals(expectValue,actualValue);
            });

        //get the customData object from request body
           CustomDataDto expectedCustomData=dealDto.getCustom_data().get(0);
           String expectedCustomDataName = expectedCustomData.getName(); //get name attribute from customdata
           String expectedCustomDataValue= expectedCustomData.getValue(); //get value attribute from customdata

         // get the customData object from response body
         CustomDataDto actualCustomData=dealResponseDto.getCustom_data().get(0);
         String actualCustomDataName = actualCustomData.getName();
        String actualCustomDataValue = actualCustomData.getValue();

         //compare actual and expected custom data name and value
        Assert.assertEquals(expectedCustomDataName,actualCustomDataName);
        Assert.assertEquals(expectedCustomDataValue,actualCustomDataValue);

        System.out.println(dealResponseDto.getOwner().getId());
        System.out.println(dealResponseDto.getOwner().getDomain());



    }
}