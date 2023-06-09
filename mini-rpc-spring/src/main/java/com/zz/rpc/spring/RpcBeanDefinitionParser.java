package com.zz.rpc.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Objects;

public class RpcBeanDefinitionParser implements BeanDefinitionParser {

    private Class<?> beanClass;

    public RpcBeanDefinitionParser(Class<?> beanClass) {
        Objects.requireNonNull(beanClass);
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition bd = new RootBeanDefinition();
        bd.setBeanClass(beanClass);
        // 不允许lazy init
        bd.setLazyInit(false);

        // 如果没有id则按照规则生成一个id,注册id到context中
        String id = element.getAttribute("id");
        if (id.length() == 0) {
            String generatedBeanName = element.getAttribute("name");
            if (generatedBeanName.length() == 0) {
                generatedBeanName = element.getAttribute("class");
            }
            if (generatedBeanName.length() == 0) {
                generatedBeanName = beanClass.getName();
            }
            id = generatedBeanName;
            int counter = 2;
            while (parserContext.getRegistry().containsBeanDefinition(id)) {
                id = generatedBeanName + (counter++);
            }
        }

        if (id.length() > 0) {
            if (parserContext.getRegistry().containsBeanDefinition(id)) {
                throw new IllegalStateException("Duplicate spring bean id " + id);
            }
            parserContext.getRegistry().registerBeanDefinition(id, bd);
        }
        bd.getPropertyValues().addPropertyValue("id", id);

        NamedNodeMap attributes = element.getAttributes();
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = attributes.item(i);
            String name = node.getLocalName();

            String value = node.getNodeValue();
            if ("ref".equals(name)) {
                if (parserContext.getRegistry().containsBeanDefinition(value)) {
                    BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(value);
                    if (!refBean.isSingleton()) {
                        throw new IllegalStateException("The exported service ref " + value + " must be singleton! Please set the " + value
                                + " bean scope to singleton, eg: <bean id=\"" + value + "\" scope=\"singleton\" ...>");
                    }
                }
                Object reference = new RuntimeBeanReference(value);
                bd.getPropertyValues().add(name, reference);
            } else {
                bd.getPropertyValues().add(name, value);
            }
        }

        return bd;
    }
}
