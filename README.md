# Testavsender

[![Build Status](https://travis-ci.org/difi/sikker-digital-post-java-eksempel-webclient.svg?branch=master)](https://travis-ci.org/difi/sikker-digital-post-java-eksempel-webclient)

> En testklient for meldingsutsending gjennom sikker digital post.

- Muliggjør funksjonell testing av meldingsformidler og postbokser via et web-grensesnitt
- Muliggjør ytelsestesting av meldingsformidler og postbokser via et web-grensesnitt
- Viser hvordan postavsendere kan ta i bruk sikker digital post og kan være et utgangspunkt for postavsenderes egne integrasjoner
- Muliggjør funksjonell testing av sending av fysisk post via meldingsformidler
- Inkluderer out-of-the-box integrasjon
- Kan prøves på http://avsender-sdp.difi.no/

## Forutsetninger

- Må bygges med Apache Maven med Java development kit (JDK) 1.8 eller nyere
- Må kjøres i Java runtime environment (JRE) 1.8 eller nyere
- Java cryptography extension (JCE) unlimited strength må være installert i JRE- se http://www.oracle.com/technetwork/java/javase/downloads/index.html

## Kjøring / installasjon

### Kjøre på Windows uten egen servlet-container

1. Last ned Git-prosjektet `git clone https://github.com/difi/sdp-klient-eksempel-java-webclient.git`
2. Start Testavsender fra roten av Git-prosjektet `start.bat`
3. Nå kjører testavsender på port 8080 - se http://localhost:8080/

### Installere på Tomcat 7 eller en annen servlet-container

1. Last ned Git-prosjektet `git clone https://github.com/difi/sdp-klient-eksempel-java-webclient.git`
2. Bygg Testavsender fra roten av Git-prosjektet `mvn clean install`
3. Installer WAR-filen som blir laget under `target/` fra roten av Git-prosjektet til ønsket servlet-container i henhold til instruksjoner for servlet-containeren

## Database

### Out-of-the-box database

Testavsender bruker H2 minnedatabase.

### Egen MySQL-database

For å ta i bruk en egen MySQL-database må en:

1. Installere MySQL-server
2. Opprette databaseskjema for testavsender i MySQL
3. Opprette bruker for testavsender i MySQL
4. Gi bruker for testavsender i MySQL full tilgang til databaseskjema for testavsender (tilgangene kan eventuelt begrenses til: SELECT, INSERT, UPDATE, DELETE, CREATE TABLE, DROP TABLE)
5. Overstyre databasekonfigurasjon (se "Overstyre standard konfigurasjon")

## Integrasjoner

### Out-of-the-box integrasjon

Testavsender er integrert mot et testmiljø for oppslagstjenesten på https://eid-inttest.difi.no/kontaktinfo-external/ws-v3. Sertifikat og annet er spesielt laget for Testavsender-integrasjonen.

Testavsender er integrert mot et testmiljø for meldingsformidler på https://qaoffentlig.meldingsformidler.digipost.no/api/. Sertifikat og annet er spesielt laget for Testavsender-integrasjonen.

### Egen integrasjon

For å sette opp egen integrasjon må en:

1. Integrere mot et testmiljø for oppsalgstjenesten - se http://begrep.difi.no/Oppslagstjenesten/
2. Integrere mot et testmiljø for meldingsformidler - se http://begrep.difi.no/SikkerDigitalPost/
3. Overstyre konfigurasjon av integrasjon (se "Overstyre standard konfigurasjon")
4. Starte testavsender på nytt

## Overstyre standard konfigurasjon

For å overstyre standard konfigurasjon må en:

1. Opprette mappen `/etc/opt/testavsender/` fra roten av operativsystemet
2. Kopiere filen `configuration.properties` fra `/src/main/resources/` fra roten av Git-prosjektet til `/etc/opt/testavsender/` fra roten av operativsystemet
3. Gjøre ønskede endringer i `configuration.properties`
4. Starte testavsender på nytt

## Ytelsestest av sikker digital post-infrastrukturen

Se https://github.com/difi/sdp-klient-eksempel-java-webclient/tree/master/performancetest/

## Mer informasjon om sikker digital post

Se http://begrep.difi.no/SikkerDigitalPost/
