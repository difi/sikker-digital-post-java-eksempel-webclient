# Ytelsestesting av sikker digital post-infrastrukturen

## Infrastruktur for ytelsestest

![Ytelsestesting av sikker digital post-infrastrukturen](https://github.com/difi/sdp-klient-eksempel-java-webclient/raw/master/performancetest/ytelsestesting-av-sikker-digital-post-infrastrukturen.png)

## Testavsenders tenester for ytelsestesting

### Sende melding med adresse-oppslag mot oppslagstjenesten

`GET /client/performance?ssn=...&size=...`

- `ssn` - fødselsnummer
- `size` - størrelse på dokumentpakke som skal sendes - `SIZE_10KB`, `SIZE_80KB`, `SIZE_800KB` eller `SIZE_8MB`

### Sende melding uten adresse-oppslag mot oppslagstjenesten

`GET /client/performance?ssn=...&size=...&postboxAddress=...&postboxVendor=...`

- `ssn` - fødselsnummer
- `size` - størrelse på dokumentpakke som skal sendes - `SIZE_10KB`, `SIZE_80KB`, `SIZE_800KB` eller `SIZE_8MB`
- `postboxAddress` - postboks-adresse
- `postboxVendor` - `EBOKS` eller `DIGIPOST`

### Laste ned rapport

Postavsender tilbyr nedlasting av en rapport som viser alle sendte meldinger inkludert blant annet meldingsstatus og tid før eventuelt mottak av kvittering.

`GET /client/report`

## Jmeter-skript for ytelsestestscenario

Jmeter er et verktøy for ytelsestesting. Les om Apache Jmeter på http://jmeter.apache.org/

Meldinger blir sendt fordelt på et stort antall testbrukere hos eBoks og Digipost.

Det er lagt inn en prosentsats som fordeler andelen meldinger som blir sendt til eBoks og Digipost i henhold til hvilken ytelse som er avtalt med den enkelte postkasseleverandør.

Det er også lagt inn en prosentsats som fordeler andelen meldinger som blir sendt av de ulike størrelsene.

Det er enkelt å bytte mellom to testscenario:

- Et "lett" scenario der meldingene inneholder dokument fordelt på 10kb (70%), 80kb (20%) eller 800kb (10%)
- Et "tungt" scenario der meldinger inneholder dokument fordelt på 10kb (70%), 800kb (20%) eller 8mb (10%)

En kan enkelt velge å kjøre testscenario med eller uten adresse-oppslag mot oppslagstjenesten

## Capistrano-skript for distribusjon og kjøring av ytelsestester

Capistrano er et verktøy for server-administrasjon. Les om Capistrano på http://capistranorb.com/

For å generere tilstrekkelig last er det nødvendig å kjøre Jmeter-skript på flere maskiner samtidig og å benytte flere installasjoner av testavsender samtidig.

Det er laget skript som gjør det enkelt å:

- Installere et vilkårlig antall instanser av testavsender og jmeter
- Distribuere testscenario og testdata
- Kjøre distribuert test
- Samle tilbake testresultat

### Installere Capistrano

1. Installere Ruby 2.0 fra http://rubyinstaller.org/downloads/
2. Installere Ruby-pakken bundler `gem install bundler`
3. Installere avhengighetene med hjelp av bundler `bundle install`

### Klargjøre og gjennomføre ytelsestest mha. Capistrano

1. Sørg for at du har SSH-tilgang til serverene du ønsker å bruke for ytelsestesting
2. Rediger `production.rb` under `/performancetest/config/deploy/` og legg inn en server med rollen controller og resten med rollen client (space-separert)
3. Legg nøkkelen som gir deg tilgang til serverene som `sshkey.pem` under `/performancetest/` (`sshkey.pem` er av sikkerhetsgrunner lagt til i `.gitignore`)
4. Nå er alt klart til å få tilgang til serverene
5. Naviger til `/performancetest/` fra roten av Git-prosjektet
6. Installer Apache Jmeter, Apache Tomcat 7 og Testavsender på alle serverer, kopier testscenario og testdata med kommandoen: `cap production yt:setup`
7. Start Jmeter-serverer med kommandoen: `cap production yt:servers:start`
8. Start test med kommandoen: `cap production yt:servers:run_test`
9. Vent til testen er ferdig
10. Last ned test-resultat med kommandoen: `cap production yt:servers:download_results`
11. Stopp Jmeter-serverer med kommandoen: `cap production yt:servers:stop`
12. Nå kan testdata lastes inn i Jmeter for analyse

Justeringer i testscenario kan distribueres uten å kjøre fullt oppsett på nytt med kommandoen `cap production yt:update`
