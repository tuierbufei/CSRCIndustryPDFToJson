package csrcIndustry;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.giaybac.traprange.PDFTableExtractor;
import com.giaybac.traprange.entity.Table;

import flexjson.JSONSerializer;

public class Program {

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {

		PDFTableExtractor extractor = (new PDFTableExtractor()).setSource("resource/companyIndustry_201603.pdf");

		extractor.exceptLine(new int[] { 0 });
		List<Table> tables = extractor.extract();

		Map<String, Map<String, Map<String, String>>> industryHashMap = new LinkedHashMap<String, Map<String, Map<String, String>>>();
		String currentIndustryName = null;
		Map<String, Map<String, String>> currentIndustryMap = null;
		Map<String, String> currentCompanyIndustry = null;
		boolean previousIndustry = false;
		String content;
		Set<String> industriesIndex = new HashSet<String>();
		for (int i = 0; i < tables.size(); i++) {
			Table table = tables.get(i);
			for (int j = 0; j < table.getRows().size(); j++) {
				if(i == 0 && j == 0) {
					continue;
				}
				
				for (int k = 0; k < table.getRows().get(j).getCells().size(); k++) {
					content = table.getRows().get(j).getCells().get(k).getContent();
					if(!content.equals("")) {
						if(k == 0 && !previousIndustry) {
							if(currentIndustryName != null && currentIndustryName.indexOf("(") != -1 && currentIndustryName.substring(0, currentIndustryName.indexOf("(")).equals(content)) {
								continue;
							}
							currentIndustryName = content;
							if(currentIndustryName.indexOf(")") == -1) {
								if(j == table.getRows().size() - 1) {
									System.out.println("第" + i + "页门类名称：" +  currentIndustryName + " 未完结");
								}
								
								previousIndustry = true;
								currentIndustryName = currentIndustryName + table.getRows().get(j + 1).getCells().get(k).getContent();
							}
							
							if(!industryHashMap.containsKey(currentIndustryName)) {
								currentIndustryMap = new LinkedHashMap<String, Map<String, String>>();
								industryHashMap.put(currentIndustryName, currentIndustryMap);
								System.out.println(currentIndustryName);
							}
						} else if(k == 0 && previousIndustry) {
							previousIndustry = false;
						}
						
						if(k == 2) {
							if(!industriesIndex.contains(content)) {
								currentCompanyIndustry = new LinkedHashMap<String, String>();
								currentIndustryMap.put(content + " " + table.getRows().get(j).getCells().get(k+1).getContent(), currentCompanyIndustry);
							}
						}
						
						if(k == 4) {
							currentCompanyIndustry.put(content, table.getRows().get(j).getCells().get( k + 1).getContent());
						}
					}
				}
			}
		}
		
		List<Industry> industries = new ArrayList<Industry>();
		for (String category : industryHashMap.keySet()) {
			Industry industry = new Industry();
			IndustryCompany[] industryCompanies = new IndustryCompany[industryHashMap.get(category).size()];
			int i = 0;
			for (String industryName : industryHashMap.get(category).keySet()) {
				Map<String, String> industryCompanyMap = industryHashMap.get(category).get(industryName);
				IndustryCompany industryCompany = new IndustryCompany();
				
				Company[] companies = new Company[industryCompanyMap.size()];
				int j = 0;
				for (String stockcd : industryCompanyMap.keySet()) {
					Company company = new Company();
					company.setCode(stockcd);
					company.setName(industryCompanyMap.get(stockcd));
					companies[j] = company;
					j++;
				}
				
				industryCompany.setName(industryName);
				industryCompany.setCompanies(companies);
				industryCompanies[i] = industryCompany;
				i++;
			}
			industry.setCategory(category);
			industry.setIndustries(industryCompanies);
			industries.add(industry);
		}
		
		JSONSerializer serializer = new JSONSerializer();
		System.out.println(serializer.exclude("*.class").deepSerialize(industries));
	}
}