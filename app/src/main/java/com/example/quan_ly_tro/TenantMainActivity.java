package com.example.quan_ly_tro;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.quan_ly_tro.ui.auth.LoginActivity;
import com.example.quan_ly_tro.ui.hoadon.HoaDonFragment;
import com.example.quan_ly_tro.ui.settings.DocumentManagerActivity;
import com.example.quan_ly_tro.ui.tenant.TenantHomeFragment;
import com.example.quan_ly_tro.ui.tenant.TenantIssueFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Màn hình chính dành riêng cho Khách thuê
 */
public class TenantMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    
    private TenantHomeFragment homeFragment;
    private HoaDonFragment invoiceFragment;
    private TenantIssueFragment issueFragment;
    
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_main);

        // Kiểm tra login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        initFragments();
        setupBottomNavigation();
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.tenant_bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void initFragments() {
        homeFragment = new TenantHomeFragment();
        invoiceFragment = new HoaDonFragment(); 
        issueFragment = new TenantIssueFragment();
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.tenant_fragment_container, issueFragment, "issue").hide(issueFragment);
        transaction.add(R.id.tenant_fragment_container, invoiceFragment, "invoice").hide(invoiceFragment);
        transaction.add(R.id.tenant_fragment_container, homeFragment, "home");
        transaction.commit();
        
        activeFragment = homeFragment;
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_tenant_home) selectedFragment = homeFragment;
            else if (itemId == R.id.nav_tenant_invoice) selectedFragment = invoiceFragment;
            else if (itemId == R.id.nav_tenant_issue) selectedFragment = issueFragment;
            else if (itemId == R.id.nav_tenant_doc) {
                startActivity(new Intent(this, DocumentManagerActivity.class));
                return false;
            }
            
            if (selectedFragment != null && selectedFragment != activeFragment) {
                fragmentManager.beginTransaction().hide(activeFragment).show(selectedFragment).commit();
                activeFragment = selectedFragment;
            }
            
            return true;
        });
    }
}
