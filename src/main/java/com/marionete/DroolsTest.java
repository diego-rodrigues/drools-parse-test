package com.marionete;

import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.io.impl.UrlResource;
import org.drools.core.rule.*;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.Constraint;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.marionete.model.Account;
import org.kie.internal.io.ResourceFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;

import java.util.Collection;

/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

    public static void inspect(RuleImpl rule){
        GroupElement.Type type = rule.getLhs().getType();
        for (RuleConditionElement rce: rule.getLhs().getChildren()) {
            if (rce instanceof Pattern) {
                if (((Pattern) rce).getConstraints().size() > 0) {
                    for (Constraint c: ((Pattern) rce).getConstraints()){
                        if (c instanceof MvelConstraint){
                            System.out.println(">>> " + ((MvelConstraint)c).getExpression());
                        }else{
                            System.out.println("Constraint type not supported.");
                        }
                    }
                }
            } else if (rce instanceof EvalCondition) {
                System.out.println(((EvalCondition)rce).getEvalExpression().toString());
            }

            for (Declaration declaration: rule.getDeclarations().values()) {
                System.out.println(declaration);
            }
        }
    }

    public static void runA(){
        try {
            // load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            KieSession kSession = kContainer.newKieSession("ksession-rules");

            KieBase kBase = kContainer.getKieBase( "rules" );

            String packageName = "demo";
            KiePackage kpkg = kBase.getKiePackage(packageName);
            Collection<Rule> rules = kpkg.getRules();

            System.out.println("Rules:");
            for (Rule r: rules) {
                System.out.println(r.getName());
//                Map<String,Object> a = r.getMetaData();
//                System.out.println(a.size());
//
//                for (Map.Entry<String,Object> entry: a.entrySet()) {
//                    System.out.println(entry.getKey());
//                }

                System.out.println(r.getId()+" "+r.getName()+"-"+r.getPackageName()+"-"+r.getMetaData());
                inspect((RuleImpl)r);
            }

            // go !
            Account account = new Account(200);
            account.withdraw(101);

            kSession.insert(account);
            kSession.fireAllRules();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void runB() {
        KieServices ks = KieServices.Factory.get();
//        KieResources resources = ks.getResources();
        String url = "http://localhost:8180/kie-server/services/rest/server/";

        UrlResource urlResource = (UrlResource) ResourceFactory.newUrlResource(url);

        urlResource.setBasicAuthentication("enabled");
        urlResource.setUsername("kieserver");
        urlResource.setPassword("kieserver1!");
        // UrlResource urlResource = (UrlResource) resources.newUrlResource(url);
        //UrlResource urlResource = new UrlResource(url);
        /*
         * urlResource.setUsername("username"); urlResource.setPassword("password");
         * urlResource.setBasicAuthentication("enabled");
         */

        ks.newKieClasspathContainer("container1");
        KieContainer kc = ks.getKieClasspathContainer("http://localhost:8180/kie-server/services/rest/server/containers/container1");
        KieBase kBase = kc.getKieBase();
        Collection<String> str = kc.getKieBaseNames();
        Collection<Rule> rules = kBase.getKiePackage("package").getRules();
    }

    public static void runC() {
        String URL = "http://localhost:8180/kie-server/services/rest/server";
        String USER = "kieserver";
        String PASSWORD = "kieserver1!";
        KieServicesConfiguration conf =
                KieServicesFactory.newRestConfiguration(URL, USER, PASSWORD);

        conf.setMarshallingFormat(MarshallingFormat.JSON);

        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(conf);

    }

    public static void main(String[] args) {
        runA();
//        runC();
    }

}
