/*******************************************************************************
 * Copyright 2013-2019 Qaprosoft (http://www.qaprosoft.com).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.qaprosoft.zafira.services.services.application.integration.context;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.qaprosoft.zafira.models.db.Setting;

import java.util.Map;

import static com.qaprosoft.zafira.models.db.Setting.SettingType.AMAZON_ACCESS_KEY;
import static com.qaprosoft.zafira.models.db.Setting.SettingType.AMAZON_BUCKET;
import static com.qaprosoft.zafira.models.db.Setting.SettingType.AMAZON_ENABLED;
import static com.qaprosoft.zafira.models.db.Setting.SettingType.AMAZON_REGION;
import static com.qaprosoft.zafira.models.db.Setting.SettingType.AMAZON_SECRET_KEY;

public class AmazonContext extends AbstractContext {

    private AmazonS3 amazonS3;
    private AWSSecurityTokenService awsSecurityTokenService;
    private BasicAWSCredentials basicAWSCredentials;
    private String s3Bucket;

    public AmazonContext(Map<Setting.SettingType, String> settings) {
        super(settings, settings.get(AMAZON_ENABLED));

        String accessKey = settings.get(AMAZON_ACCESS_KEY);
        String secretKey = settings.get(AMAZON_SECRET_KEY);
        String bucket = settings.get(AMAZON_BUCKET);
        String region = settings.get(AMAZON_REGION);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxConnections(100);
        clientConfiguration.setProtocol(Protocol.HTTPS);

        this.s3Bucket = bucket;
        this.basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(Regions.fromName(region))
                .withClientConfiguration(clientConfiguration).build();
        this.awsSecurityTokenService = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(Regions.fromName(region))
                .withClientConfiguration(clientConfiguration).build();
    }

    public AmazonS3 getAmazonS3() {
        return amazonS3;
    }

    public void setAmazonS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public AWSSecurityTokenService getAwsSecurityTokenService() {
        return awsSecurityTokenService;
    }

    public void setAwsSecurityTokenService(AWSSecurityTokenService awsSecurityTokenService) {
        this.awsSecurityTokenService = awsSecurityTokenService;
    }

    public BasicAWSCredentials getBasicAWSCredentials() {
        return basicAWSCredentials;
    }

    public void setBasicAWSCredentials(BasicAWSCredentials basicAWSCredentials) {
        this.basicAWSCredentials = basicAWSCredentials;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }
}
